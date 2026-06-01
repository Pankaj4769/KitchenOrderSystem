package com.kos.authentication;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Server-side index of issued refresh tokens, keyed by their JWT ID (JTI).
 *
 * <p>Why this exists: a JWT signature alone tells us the token was issued by
 * us, but it cannot tell us whether the token has since been logged out,
 * rotated, or otherwise revoked. By tracking each refresh token's JTI here
 * we can invalidate individual tokens immediately.
 *
 * <p><b>In-memory limitation:</b> entries live in a {@link ConcurrentHashMap}
 * inside one JVM. This means:
 * <ul>
 *   <li>A JVM restart drops every refresh token — all logged-in users are
 *       forced to re-login on next access-token expiry. Acceptable for a
 *       single-instance deployment that rarely restarts.</li>
 *   <li>If the backend is ever scaled horizontally, refresh tokens issued by
 *       instance A cannot be validated by instance B. Migrate to Redis at
 *       that point — the public surface of this class is intentionally small
 *       so swapping the backing store is a localised change.</li>
 * </ul>
 */
@Component
public class RefreshTokenStore {

    private static final Logger logger = LogManager.getLogger(RefreshTokenStore.class);

    private final Map<String, Record> store = new ConcurrentHashMap<>();

    public static final class Record {
        public final String username;
        public final Instant expiresAt;
        Record(String username, Instant expiresAt) {
            this.username  = username;
            this.expiresAt = expiresAt;
        }
        boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    }

    public void register(String jti, String username, long ttlMs) {
        if (jti == null || username == null) return;
        store.put(jti, new Record(username, Instant.now().plusMillis(ttlMs)));
    }

    /** True if a refresh token with this JTI exists, belongs to {@code username}, and isn't expired. */
    public boolean isValid(String jti, String username) {
        if (jti == null || username == null) return false;
        Record r = store.get(jti);
        if (r == null) return false;
        if (r.isExpired()) {
            store.remove(jti);
            return false;
        }
        return username.equals(r.username);
    }

    /** Idempotent — silent no-op if the JTI is already gone. */
    public void revoke(String jti) {
        if (jti == null) return;
        store.remove(jti);
    }

    /** Removes every token belonging to {@code username} (e.g. "logout from all devices"). */
    public int revokeAllForUser(String username) {
        if (username == null) return 0;
        int removed = 0;
        Iterator<Map.Entry<String, Record>> it = store.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Record> e = it.next();
            if (username.equals(e.getValue().username)) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }

    /** Periodic sweep of expired entries so the map doesn't grow unbounded. */
    @Scheduled(fixedDelay = 60 * 60 * 1000L) // hourly
    void cleanupExpired() {
        int before = store.size();
        store.values().removeIf(Record::isExpired);
        int removed = before - store.size();
        if (removed > 0) {
            logger.info("RefreshTokenStore: cleaned {} expired entries, {} remain", removed, store.size());
        }
    }

    /** For tests/diagnostics. */
    int size() { return store.size(); }
}
