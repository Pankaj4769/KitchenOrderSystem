package com.kos.authentication;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Configuration
public class PasswordUtil {

    private static final int BCRYPT_COST = 12;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_COST);
    }

    @Component
    public static class PasswordHelper {

        private final PasswordEncoder passwordEncoder;

        public PasswordHelper(PasswordEncoder passwordEncoder) {
            this.passwordEncoder = passwordEncoder;
        }

        public String encode(String hashedFromClient) {
            return passwordEncoder.encode(hashedFromClient);
        }

        public boolean matches(String hashedFromClient, String storedValue) {
            if (storedValue == null || hashedFromClient == null) {
                return false;
            }
            if (isBcrypt(storedValue)) {
                return passwordEncoder.matches(hashedFromClient, storedValue);
            }
            // Legacy migration path: DB still has plaintext from before encryption rollout.
            // Match against SHA-256 of the stored plaintext, or against the raw plaintext
            // itself (so an admin testing via Postman with the old plaintext still works).
            return hashedFromClient.equalsIgnoreCase(sha256(storedValue))
                    || hashedFromClient.equals(storedValue);
        }

        public boolean isBcrypt(String value) {
            return value != null && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
        }

        public String sha256(String input) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
                StringBuilder hex = new StringBuilder(bytes.length * 2);
                for (byte b : bytes) {
                    hex.append(String.format("%02x", b));
                }
                return hex.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 not available", e);
            }
        }
    }
}
