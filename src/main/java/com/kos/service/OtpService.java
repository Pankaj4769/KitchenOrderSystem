package com.kos.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.kos.dto.AuthUser;

@Service
public class OtpService {

    private static final Logger logger = LogManager.getLogger(OtpService.class);

    private static final long OTP_VALIDITY_SECONDS = 300;          // 5 min
    private static final long VERIFIED_TTL_SECONDS  = 600;          // 10 min

    private static class OtpEntry {
        final String otp;
        final Instant expiry;
        OtpEntry(String otp) {
            this.otp    = otp;
            this.expiry = Instant.now().plusSeconds(OTP_VALIDITY_SECONDS);
        }
        boolean isExpired() { return Instant.now().isAfter(expiry); }
    }

    // key: "identifierType:identifier"
    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();

    // outer key: username (JWT subject). inner key: lowercased identifier. value: verified-at instant.
    private final Map<String, Map<String, Instant>> verifiedRecently = new ConcurrentHashMap<>();

    @Autowired private JavaMailSender mailSender;
    @Autowired private UserService userService;
    @Autowired(required = false) private SmsOtpService smsOtpService;

    public boolean sendOtp(String identifier, String identifierType) {
        logger.info("Entering sendOtp() type={} id={}", identifierType, identifier);
        try {
            String otp = "123456"; // dev convention (see KOS auth.service notes)
            store.put(key(identifierType, identifier), new OtpEntry(otp));

            if ("mobile".equalsIgnoreCase(identifierType)) {
                boolean ok = smsOtpService != null && smsOtpService.sendOtp(identifier, otp);
                if (!ok) store.remove(key(identifierType, identifier));
                return ok;
            }

            // email or username → resolve recipient email
            String email;
            Optional<AuthUser> optUser = userService.getUserByIdentifier(identifier, identifierType);
            if (optUser.isPresent()) {
                email = optUser.get().getEmail();
                if (email == null || email.isBlank()) { return false; }
            } else if ("email".equalsIgnoreCase(identifierType)) {
                email = identifier.trim();
            } else {
                return false;
            }

            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom("noreply@tech2software.com");
                msg.setTo(email);
                msg.setSubject("KOS - OTP Verification");
                msg.setText("Your OTP is: " + otp + "\n\nThis OTP is valid for 5 minutes.");
                mailSender.send(msg);
                return true;
            } catch (Exception e) {
                logger.error("Failed to send OTP email: {}", e.getMessage(), e);
                store.remove(key(identifierType, identifier));
                return false;
            }
        } finally {
            logger.info("Exiting sendOtp()");
        }
    }

    public boolean verifyOtp(String identifier, String identifierType, String otp) {
        logger.info("Entering verifyOtp()");
        try {
            OtpEntry entry = store.get(key(identifierType, identifier));
            if (entry == null || entry.isExpired())          return false;
            if (!entry.otp.equals(otp.trim()))               return false;
            store.remove(key(identifierType, identifier));   // consume
            return true;
        } finally {
            logger.info("Exiting verifyOtp()");
        }
    }

    // ── Verified-recently tracking ─────────────────────────────────────────────

    public void markVerified(String username, String identifier) {
        if (username == null || identifier == null) return;
        verifiedRecently
            .computeIfAbsent(username, k -> new ConcurrentHashMap<>())
            .put(identifier.trim().toLowerCase(), Instant.now());
    }

    public boolean wasRecentlyVerified(String username, String identifier) {
        if (username == null || identifier == null) return false;
        Map<String, Instant> map = verifiedRecently.get(username);
        if (map == null) return false;
        Instant at = map.get(identifier.trim().toLowerCase());
        if (at == null) return false;
        if (Instant.now().isAfter(at.plusSeconds(VERIFIED_TTL_SECONDS))) {
            map.remove(identifier.trim().toLowerCase());
            return false;
        }
        return true;
    }

    public void clearVerification(String username, String identifier) {
        if (username == null || identifier == null) return;
        Map<String, Instant> map = verifiedRecently.get(username);
        if (map != null) map.remove(identifier.trim().toLowerCase());
    }

    private String key(String identifierType, String identifier) {
        return identifierType.toLowerCase() + ":" + identifier.trim().toLowerCase();
    }
}
