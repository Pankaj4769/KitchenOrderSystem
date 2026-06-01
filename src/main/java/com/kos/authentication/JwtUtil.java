package com.kos.authentication;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    public static final long ACCESS_TTL_MS  = 1000L * 60 * 15;        // 15 min — short-lived session cookie
    public static final long REFRESH_TTL_MS = 1000L * 60 * 60 * 24 * 7; // 7 days
    public static final long SSO_TTL_MS     = 1000L * 60;             // 60 seconds

    /** @deprecated kept temporarily for callers still referencing the legacy 1h TTL. */
    @Deprecated
    public static final long DEFAULT_TTL_MS = ACCESS_TTL_MS;

    @Value("${app.jwt.secret:}")
    private String secret;

    private Key signingKey;

    @PostConstruct
    void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                "JWT secret is not configured. Set the JWT_SECRET environment variable " +
                "(or the app.jwt.secret property). HS256 requires at least 32 bytes.");
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                "JWT secret must be at least 32 bytes for HS256. Provided length: " + bytes.length);
        }
        this.signingKey = Keys.hmacShaKeyFor(bytes);
    }

    /** Legacy single-arg generator. Prefer {@link #generateToken(String, String, String, String)}. */
    public String generateToken(String username) {
        return generateToken(username, null, null, null);
    }

    public String generateToken(String username, String role, String restaurantId, String staffId) {
        Map<String, Object> claims = new HashMap<>();
        if (role != null)         claims.put("role", role);
        if (restaurantId != null) claims.put("restaurantId", restaurantId);
        if (staffId != null)      claims.put("staffId", staffId);
        return buildToken(username, claims, ACCESS_TTL_MS, null);
    }

    /**
     * Long-lived token used solely by /auth/refresh to mint new access tokens.
     * Carries a JTI (JWT ID) so the server-side {@code RefreshTokenStore} can
     * revoke an individual token (e.g. on logout or after rotation).
     */
    public String generateRefreshToken(String username, String jti) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", "refresh");
        return buildToken(username, claims, REFRESH_TTL_MS, jti);
    }

    /**
     * Short-lived (60s) token used for cross-app handoff (e.g. the employee-mgmt
     * SSO link in the sidebar). Carries the same identity claims as a normal
     * token but is signalled by a "purpose=sso" claim so the receiving app can
     * choose to scope-limit it.
     */
    public String generateSsoToken(String username, String role, String restaurantId, String staffId) {
        Map<String, Object> claims = new HashMap<>();
        if (role != null)         claims.put("role", role);
        if (restaurantId != null) claims.put("restaurantId", restaurantId);
        if (staffId != null)      claims.put("staffId", staffId);
        claims.put("purpose", "sso");
        return buildToken(username, claims, SSO_TTL_MS, null);
    }

    private String buildToken(String subject, Map<String, Object> claims, long ttlMs, String jti) {
        long now = System.currentTimeMillis();
        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttlMs));
        if (jti != null) builder.setId(jti);
        return builder
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public Claims extractClaims(String token) {
        return parse(token);
    }

    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
