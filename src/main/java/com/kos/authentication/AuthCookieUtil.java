package com.kos.authentication;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Builds the session cookie. Profile-aware: in local/dev the cookie is non-Secure
 * so it works over plain HTTP; in prod it is Secure + SameSite=Lax.
 *
 * Lax is chosen (not Strict) so that the cookie is sent when a logged-in user
 * arrives via a bookmark or external link — Strict would treat that as a
 * cross-site navigation and the SPA would briefly look logged-out before
 * redirecting through /login.
 */
@Component
public class AuthCookieUtil {

    public static final String COOKIE_NAME         = "kos_session";
    public static final String REFRESH_COOKIE_NAME = "kos_refresh";

    @Value("${app.cookie.secure:false}")
    private boolean secure;

    /** Cookie domain — leave blank in dev to default to the request host. */
    @Value("${app.cookie.domain:}")
    private String domain;

    public ResponseCookie buildSessionCookie(String token, long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSeconds));
        if (domain != null && !domain.isBlank()) {
            b.domain(domain);
        }
        return b.build();
    }

    public ResponseCookie buildClearCookie() {
        return buildClearCookie(COOKIE_NAME);
    }

    /**
     * Refresh-token cookie. Same attributes as the session cookie except for
     * the name. Scoped to {@code /} (rather than e.g. {@code /auth/refresh}) so
     * /auth/logout can also clear/revoke it.
     */
    public ResponseCookie buildRefreshCookie(String token, long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(REFRESH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSeconds));
        if (domain != null && !domain.isBlank()) {
            b.domain(domain);
        }
        return b.build();
    }

    public ResponseCookie buildClearRefreshCookie() {
        return buildClearCookie(REFRESH_COOKIE_NAME);
    }

    private ResponseCookie buildClearCookie(String name) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0);
        if (domain != null && !domain.isBlank()) {
            b.domain(domain);
        }
        return b.build();
    }

    public String extractTokenFromCookie(HttpServletRequest request) {
        return extractCookieValue(request, COOKIE_NAME);
    }

    public String extractRefreshTokenFromCookie(HttpServletRequest request) {
        return extractCookieValue(request, REFRESH_COOKIE_NAME);
    }

    private String extractCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                String v = c.getValue();
                return (v == null || v.isEmpty()) ? null : v;
            }
        }
        return null;
    }
}
