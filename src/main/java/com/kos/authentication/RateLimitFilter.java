package com.kos.authentication;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Per-IP rate limiting on sensitive auth endpoints. Runs before
 * {@link JwtAuthFilter} so rejected requests never reach the JWT logic.
 *
 * <p>Limits are deliberately generous for legitimate users (a cashier may
 * legitimately mistype their password a few times) but tight enough to make
 * credential stuffing and OTP brute force impractical.
 *
 * <p>Per-username rate limiting (which would defeat IP rotation) is NOT
 * implemented here because reading the JSON body inside a filter requires
 * wrapping the request for re-reading — added complexity for a marginal
 * benefit on a single-tenant POS. Add if a real attacker shows up.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // before JwtAuthFilter
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LogManager.getLogger(RateLimitFilter.class);

    private final RateLimiter rateLimiter;
    private final ClientIpResolver clientIpResolver;
    private final UrlPathHelper pathHelper = new UrlPathHelper();

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    public RateLimitFilter(RateLimiter rateLimiter, ClientIpResolver clientIpResolver) {
        this.rateLimiter = rateLimiter;
        this.clientIpResolver = clientIpResolver;
        this.pathHelper.setRemoveSemicolonContent(true);
        this.pathHelper.setUrlDecode(true);
    }

    /** Single source of truth for which endpoints get which budget. */
    private static final List<Policy> POLICIES = List.of(
            // Login: 5 allowed before lockout, then 2-min wait per retry
            // (capacity / window = 5 / 10min = 1 token per 2 min).
            new Policy("POST", path -> path.equals("/auth/login"),            5, Duration.ofMinutes(10)),
            new Policy("POST", path -> path.equals("/auth/loginWithOtp"),     5, Duration.ofMinutes(10)),

            // OTP send: 3 per 15 min (SMS/email cost) + IP cap. Spam protection.
            new Policy("POST", path -> path.equals("/auth/sendOtp"),          3, Duration.ofMinutes(15)),

            // OTP verify: matches the per-OTP attempt cap in OtpService — additional IP-level shield.
            new Policy("POST", path -> path.equals("/auth/verifyOtp"),        5, Duration.ofMinutes(5)),

            // Password reset flows.
            new Policy("PUT",  path -> path.equals("/auth/forgotPassword"),   3, Duration.ofMinutes(15)),
            new Policy("PUT",  path -> path.equals("/auth/resetTempPassword"), 5, Duration.ofMinutes(15)),

            // Signup: rare event; tight cap discourages account farming.
            new Policy("POST", path -> path.equals("/auth/signUp"),           3, Duration.ofHours(1)),

            // Token endpoints. Refresh runs ~once every 15min per active user, so
            // 30/min easily covers a tab with many parallel requests; SSO is rare.
            new Policy("POST", path -> path.equals("/auth/refresh"),         30, Duration.ofMinutes(1)),
            new Policy("POST", path -> path.equals("/auth/ssoToken"),        10, Duration.ofMinutes(1)),

            // User-existence enumeration endpoints (used during signup typing).
            // Generous-ish for honest typing, blocks scripted enumeration.
            new Policy("GET",  path -> path.startsWith("/auth/getUser/"),     20, Duration.ofMinutes(1)),
            new Policy("GET",  path -> path.startsWith("/auth/getUserByEmail/"),  20, Duration.ofMinutes(1)),
            new Policy("GET",  path -> path.startsWith("/auth/getUserByMobile/"), 20, Duration.ofMinutes(1))
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }

        String path;
        try {
            path = pathHelper.getPathWithinApplication(request);
        } catch (Exception e) {
            // Malformed path — let JwtAuthFilter handle the rejection.
            chain.doFilter(request, response);
            return;
        }
        String method = request.getMethod();

        Policy matched = null;
        for (Policy p : POLICIES) {
            if (p.matches(method, path)) { matched = p; break; }
        }
        if (matched == null) {
            chain.doFilter(request, response);
            return;
        }

        String ip = clientIpResolver.resolve(request);
        String key = method + " " + matched.matchKey(path) + " :: " + ip;
        long waitMs = rateLimiter.tryAcquire(key, matched.capacity, matched.window);
        if (waitMs == 0) {
            chain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1, (waitMs + 999) / 1000);
        logger.warn("Rate limit hit: key={} retryAfter={}s", key, retryAfterSeconds);

        // Because this filter runs before Spring Security's CORS handler, we
        // must echo the CORS headers manually — otherwise the browser blocks
        // the 429 response from reaching the SPA and shows a CORS error
        // instead. The origin echo matches the SecurityConfig allow-list.
        applyCorsHeaders(request, response);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
        response.setHeader("Access-Control-Expose-Headers", "Retry-After");
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"message\":\"Too many requests. Try again in " + retryAfterSeconds + " seconds.\","
                + "\"status\":429,\"retryAfter\":" + retryAfterSeconds + "}");
    }

    private static final java.util.Set<String> ALLOWED_ORIGINS = java.util.Set.of(
            "http://localhost:4200",
            "https://api.tech2kitchen.com",
            "https://www.tech2kitchen.com"
    );

    private void applyCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Vary", "Origin");
        }
    }

    private static final class Policy {
        final String method;
        final java.util.function.Predicate<String> pathMatcher;
        final int capacity;
        final Duration window;

        Policy(String method, java.util.function.Predicate<String> pathMatcher, int capacity, Duration window) {
            this.method = method;
            this.pathMatcher = pathMatcher;
            this.capacity = capacity;
            this.window = window;
        }
        boolean matches(String requestMethod, String requestPath) {
            return method.equalsIgnoreCase(requestMethod) && pathMatcher.test(requestPath);
        }
        /** Key prefix used in the bucket map — strips the variable {username/email/mobile} segment. */
        String matchKey(String path) {
            // /auth/getUser/{x} → /auth/getUser/*
            int idx = path.indexOf("/auth/getUser");
            if (idx == 0)                                     return "/auth/getUser/*";
            if (path.startsWith("/auth/getUserByEmail/"))     return "/auth/getUserByEmail/*";
            if (path.startsWith("/auth/getUserByMobile/"))    return "/auth/getUserByMobile/*";
            return path;
        }
    }
}
