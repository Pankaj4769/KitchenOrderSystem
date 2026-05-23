package com.kos.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
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

    private static final long OTP_VALIDITY_SECONDS = 300; // 5 minutes

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

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserService userService;

    public boolean sendOtp(String identifier, String identifierType) {
        logger.info("Entering sendOtp()");
        try {
            String email;

            Optional<AuthUser> optUser = userService.getUserByIdentifier(identifier, identifierType);
            if (optUser.isPresent()) {
                // Existing user (forgot password flow) — use their registered email
                email = optUser.get().getEmail();
                if (email == null || email.isBlank()) {
                    logger.info("Exiting sendOtp()");
                    return false;
                }
            } else if ("email".equalsIgnoreCase(identifierType)) {
                // No user found but identifier is an email — allow signup flow
                email = identifier.trim();
            } else {
                // username/mobile with no matching user — reject
                logger.info("Exiting sendOtp()");
                return false;
            }

            //String otp = String.format("%06d", new Random().nextInt(1_000_000));
            String otp = "123456";
            store.put(key(identifierType, identifier), new OtpEntry(otp));

            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom("noreply@tech2software.com");
                msg.setTo(email);
                msg.setSubject("KOS - OTP Verification");
                msg.setText("Your OTP is: " + otp + "\n\nThis OTP is valid for 5 minutes.");
                mailSender.send(msg);
                logger.info("Exiting sendOtp()");
                return true;
            } catch (Exception e) {
                logger.error("Failed to send OTP email: {}", e.getMessage(), e);
                store.remove(key(identifierType, identifier));
                logger.info("Exiting sendOtp()");
                return false;
            }
        } catch (RuntimeException e) {
            logger.error("Error in sendOtp(): {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean verifyOtp(String identifier, String identifierType, String otp) {
        logger.info("Entering verifyOtp()");
        try {
            OtpEntry entry = store.get(key(identifierType, identifier));
            if (entry == null || entry.isExpired()) {
                logger.info("Exiting verifyOtp()");
                return false;
            }
            if (!entry.otp.equals(otp.trim())) {
                logger.info("Exiting verifyOtp()");
                return false;
            }
            store.remove(key(identifierType, identifier)); // consume OTP
            logger.info("Exiting verifyOtp()");
            return true;
        } catch (RuntimeException e) {
            logger.error("Error in verifyOtp(): {}", e.getMessage(), e);
            throw e;
        }
    }

    private String key(String identifierType, String identifier) {
        return identifierType.toLowerCase() + ":" + identifier.trim().toLowerCase();
    }
}
