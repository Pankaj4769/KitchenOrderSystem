package com.kos.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OtpServiceTest {

    @Test
    void wasRecentlyVerified_returnsFalseWhenNotRecorded() {
        OtpService svc = new OtpService();
        assertFalse(svc.wasRecentlyVerified("user1", "user1@example.com"));
    }

    @Test
    void markVerified_thenWasRecentlyVerified_returnsTrue() {
        OtpService svc = new OtpService();
        svc.markVerified("user1", "user1@example.com");
        assertTrue(svc.wasRecentlyVerified("user1", "user1@example.com"));
    }

    @Test
    void clearVerification_removesEntry() {
        OtpService svc = new OtpService();
        svc.markVerified("user1", "user1@example.com");
        svc.clearVerification("user1", "user1@example.com");
        assertFalse(svc.wasRecentlyVerified("user1", "user1@example.com"));
    }

    @Test
    void wasRecentlyVerified_isCaseInsensitiveOnIdentifier() {
        OtpService svc = new OtpService();
        svc.markVerified("user1", "USER1@EXAMPLE.com");
        assertTrue(svc.wasRecentlyVerified("user1", "user1@example.com"));
    }
}
