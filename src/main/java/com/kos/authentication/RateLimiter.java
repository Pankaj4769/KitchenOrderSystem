package com.kos.authentication;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Thread-safe token-bucket rate limiter, keyed by an arbitrary string
 * (typically "<endpoint>:<clientIp>"). Each unique key gets its own bucket.
 *
 * <p>Algorithm: classic token bucket. A bucket starts full ({@code capacity}
 * tokens). Tokens regenerate at the rate of {@code capacity / windowMs}, so a
 * configuration like "5 requests per minute" means the bucket refills one
 * token every 12 seconds. A request consumes one token; if none are
 * available, the request is denied and the caller is told how many
 * milliseconds to wait before retrying.
 *
 * <p><b>In-memory only.</b> Same limitations as {@link RefreshTokenStore}:
 * restart clears all counters; horizontal scaling lets an attacker hop
 * between instances. Acceptable for single-instance deployments.
 */
@Component
public class RateLimiter {

    private static final Logger logger = LogManager.getLogger(RateLimiter.class);

    /** Buckets evicted after this long with no activity, to bound memory. */
    private static final long IDLE_EVICTION_MS = Duration.ofHours(1).toMillis();

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Attempts to consume one token from the bucket identified by {@code key}.
     * Returns 0 if allowed, or the number of milliseconds the caller should
     * wait before the next token becomes available.
     */
    public long tryAcquire(String key, int capacity, Duration window) {
        Bucket b = buckets.computeIfAbsent(key, k -> new Bucket(capacity, window.toMillis()));
        return b.tryConsume();
    }

    /** For tests / diagnostics. */
    public void reset(String key) { buckets.remove(key); }

    @Scheduled(fixedDelay = 30 * 60 * 1000L) // every 30 min
    void evictIdle() {
        long cutoff = System.currentTimeMillis() - IDLE_EVICTION_MS;
        int before = buckets.size();
        Iterator<Map.Entry<String, Bucket>> it = buckets.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().lastTouched() < cutoff) it.remove();
        }
        int removed = before - buckets.size();
        if (removed > 0) {
            logger.info("RateLimiter: evicted {} idle buckets, {} remain", removed, buckets.size());
        }
    }

    /**
     * One bucket per key. Synchronised on the bucket's own lock — fine-grained
     * so different keys (e.g. different IPs) don't contend.
     */
    private static final class Bucket {
        private final int capacity;
        private final long windowMs;
        private final ReentrantLock lock = new ReentrantLock();
        private double tokens;
        private long lastRefillNanos;
        private volatile long lastTouchedMs;

        Bucket(int capacity, long windowMs) {
            this.capacity = capacity;
            this.windowMs = windowMs;
            this.tokens = capacity;
            this.lastRefillNanos = System.nanoTime();
            this.lastTouchedMs = System.currentTimeMillis();
        }

        long tryConsume() {
            lock.lock();
            try {
                long now = System.nanoTime();
                double tokensPerNano = (double) capacity / (windowMs * 1_000_000.0);
                tokens = Math.min(capacity, tokens + (now - lastRefillNanos) * tokensPerNano);
                lastRefillNanos = now;
                lastTouchedMs = System.currentTimeMillis();
                if (tokens >= 1.0) {
                    tokens -= 1.0;
                    return 0;
                }
                double tokensNeeded = 1.0 - tokens;
                double nanosToWait = tokensNeeded / tokensPerNano;
                return Math.max(1L, (long) (nanosToWait / 1_000_000.0));
            } finally {
                lock.unlock();
            }
        }

        long lastTouched() { return lastTouchedMs; }
    }
}
