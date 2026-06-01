package com.kos.authentication;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Best-effort client IP detection. Prefers {@code X-Forwarded-For} (set by
 * the reverse proxy in front of the app) over {@code request.getRemoteAddr()}
 * which would otherwise return the proxy's IP.
 *
 * <p><b>Trust caveat:</b> we trust {@code X-Forwarded-For} unconditionally.
 * That's safe behind a configured reverse proxy that strips client-supplied
 * copies of the header, but unsafe if the app is directly internet-exposed —
 * a hostile client could spoof any IP. Set up Hostinger/CloudFlare to
 * overwrite the header before deploying behind it.
 */
@Component
public class ClientIpResolver {

    public String resolve(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // Leftmost entry is the original client.
            int comma = xff.indexOf(',');
            String first = (comma > 0 ? xff.substring(0, comma) : xff).trim();
            if (!first.isEmpty()) return first;
        }
        String real = request.getHeader("X-Real-IP");
        if (real != null && !real.isBlank()) return real.trim();
        String remote = request.getRemoteAddr();
        return remote != null ? remote : "unknown";
    }
}
