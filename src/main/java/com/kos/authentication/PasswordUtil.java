package com.kos.authentication;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
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

        /**
         * When true: the UI is expected to send already-SHA256-hashed passwords;
         * the backend rejects anything that doesn't match the structural format.
         * When false: the UI sends plaintext; the backend SHA256s it before
         * BCrypting for storage. Either way, the stored value is
         * BCrypt(sha256(plaintext)) so logins keep working across toggles.
         */
        @Value("${app.password.encryption.enabled:true}")
        private boolean clientSideEncryptionEnabled;

        /**
         * Pre-computed BCrypt hash used purely to burn CPU when a login attempt
         * targets a non-existent user — this keeps the response latency for
         * "user not found" the same as "user exists, wrong password", so an
         * attacker cannot enumerate usernames by timing the response.
         *
         * The actual plaintext doesn't matter; what matters is that any
         * matches() call against this hash returns false in roughly the same
         * time as a real BCrypt compare.
         */
        private final String dummyHash;

        public PasswordHelper(PasswordEncoder passwordEncoder) {
            this.passwordEncoder = passwordEncoder;
            this.dummyHash = passwordEncoder.encode("dummy-value-for-constant-time-login");
        }

        /**
         * Normalises whatever the client sent into the canonical
         * SHA-256-hex form that downstream code (matches, encode) expects.
         *
         * <ul>
         *   <li>If client-side encryption is ON: the input must already be a
         *       lowercase 64-char hex SHA-256 digest. Returns it as-is, or
         *       {@code null} if the format is wrong (caller decides whether
         *       to 400/401).</li>
         *   <li>If client-side encryption is OFF: the input is treated as
         *       plaintext and SHA-256ed here. {@code null} or blank → {@code null}.</li>
         * </ul>
         */
        public String canonicalize(String incoming) {
            if (incoming == null || incoming.isEmpty()) return null;
            if (clientSideEncryptionEnabled) {
                return isLikelySha256Hex(incoming) ? incoming : null;
            }
            return sha256(incoming);
        }

        public boolean isClientSideEncryptionEnabled() {
            return clientSideEncryptionEnabled;
        }

        /** Constant-time burn: always returns false, takes ~same time as a real match. */
        public void burnCycles(String candidateHash) {
            try {
                passwordEncoder.matches(candidateHash == null ? "" : candidateHash, dummyHash);
            } catch (Exception ignored) {
                // matches() shouldn't throw on a valid BCrypt hash, but if it does
                // the only impact is timing variance — swallow.
            }
        }

        /**
         * The UI is required to send passwords as a lowercase hex SHA-256 digest
         * (64 chars, [0-9a-f]). This is a structural sanity check — it catches
         * misbehaving clients and rejects raw plaintext passwords from bypass
         * attempts. It does NOT verify password complexity (the original
         * plaintext could still be "a"); complexity must be enforced client-side
         * because the backend never sees the original.
         */
        public boolean isLikelySha256Hex(String value) {
            if (value == null || value.length() != 64) return false;
            for (int i = 0; i < 64; i++) {
                char c = value.charAt(i);
                boolean ok = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
                if (!ok) return false;
            }
            return true;
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
