package com.kos.authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.jsonwebtoken.Claims;

import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthCookieUtil authCookieUtil;
    private final UrlPathHelper pathHelper = new UrlPathHelper();

    public JwtAuthFilter(JwtUtil jwtUtil, AuthCookieUtil authCookieUtil) {
        this.jwtUtil = jwtUtil;
        this.authCookieUtil = authCookieUtil;
        // Strip path-parameter segments (e.g. ;jsessionid=...) before path comparison.
        this.pathHelper.setRemoveSemicolonContent(true);
        this.pathHelper.setUrlDecode(true);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Use the normalized path inside the application (URL-decoded, semicolons removed,
        // servlet-context prefix stripped). The raw URI from getRequestURI() can be
        // crafted to bypass startsWith() checks via %70rofile, ../, or ;jsessionid= tricks.
        String rawUri = request.getRequestURI();
        String path;
        try {
            path = pathHelper.getPathWithinApplication(request);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Malformed request path\",\"status\":400}");
            return;
        }

        // Defense-in-depth: reject any path that still contains traversal segments,
        // percent-encoding, or path parameters after normalization. These shouldn't
        // appear in a normalized path; if they do, treat the request as hostile.
        if (path == null
                || path.contains("..")
                || path.contains("%")
                || path.contains(";")
                || rawUri.contains("..")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Malformed request path\",\"status\":400}");
            return;
        }

        // /auth/verifyOtp: parses JWT if present but doesn't require it
        // (forgot-password flow calls it anonymously; settings flow calls it authenticated).
        boolean bestEffortJwt = path.equals("/auth/verifyOtp");

        // Auth-required /auth/** endpoints — JWT must be present and valid.
        boolean authRequiredAuthPath =
                path.startsWith("/auth/profile/")
                || path.equals("/auth/ssoToken");

        // Public paths skip JWT entirely. Use startsWith only on the normalized
        // path; never on the raw URI.
        boolean skipFilter =
                !bestEffortJwt &&
                !authRequiredAuthPath &&
                (path.startsWith("/auth/") || path.equals("/order-stream") || path.startsWith("/restaurant-images/"));

        if (skipFilter) {
            filterChain.doFilter(request, response);
            return;
        }

        // Prefer cookie (httpOnly session) over Authorization header.
        // The header is kept for non-browser clients (Postman, curl, internal tools).
        String token = authCookieUtil.extractTokenFromCookie(request);
        if (token == null) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                token = header.substring(7);
            }
        }

        if (token == null) {
            if (bestEffortJwt) {
                filterChain.doFilter(request, response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Missing or invalid token\",\"status\":401}");
            return;
        }

        if (!jwtUtil.validateToken(token)) {
            if (bestEffortJwt) {
                filterChain.doFilter(request, response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Invalid token\",\"status\":401}");
            return;
        }

        Claims claims = jwtUtil.extractClaims(token);
        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        // Expose tenant + staff identity from the token so controllers don't
        // have to trust a client-controlled header.
        Object restaurantId = claims.get("restaurantId");
        Object staffId = claims.get("staffId");
        if (restaurantId != null) request.setAttribute("jwt.restaurantId", restaurantId.toString());
        if (staffId != null)      request.setAttribute("jwt.staffId", staffId.toString());

        List<GrantedAuthority> authorities = role != null
                ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                : Collections.emptyList();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}


