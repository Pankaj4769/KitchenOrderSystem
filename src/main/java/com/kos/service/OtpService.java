package com.kos.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.kos.dto.AuthUser;

@Service
public class OtpService {

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
        String email;

        Optional<AuthUser> optUser = userService.getUserByIdentifier(identifier, identifierType);
        if (optUser.isPresent()) {
            // Existing user (forgot password flow) — use their registered email
            email = optUser.get().getEmail();
            if (email == null || email.isBlank()) return false;
        } else if ("email".equalsIgnoreCase(identifierType)) {
            // No user found but identifier is an email — allow signup flow
            email = identifier.trim();
        } else {
            // username/mobile with no matching user — reject
            return false;
        }

        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        store.put(key(identifierType, identifier), new OtpEntry(otp));

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("noreply@tech2software.com");
            msg.setTo(email);
            msg.setSubject("KOS - OTP Verification");
            msg.setText("Your OTP is: " + otp + "\n\nThis OTP is valid for 5 minutes.");
            mailSender.send(msg);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
            store.remove(key(identifierType, identifier));
            return false;
        }
    }

    public boolean verifyOtp(String identifier, String identifierType, String otp) {
        OtpEntry entry = store.get(key(identifierType, identifier));
        if (entry == null || entry.isExpired()) return false;
        if (!entry.otp.equals(otp.trim())) return false;
        store.remove(key(identifierType, identifier)); // consume OTP
        return true;
    }

    private String key(String identifierType, String identifier) {
        return identifierType.toLowerCase() + ":" + identifier.trim().toLowerCase();
    }
}
