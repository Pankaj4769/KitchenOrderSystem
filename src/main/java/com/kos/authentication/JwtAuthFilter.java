package com.kos.authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UrlPathHelper pathHelper = new UrlPathHelper();

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
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

        // Public paths skip JWT entirely. /auth/profile/** is EXCLUDED — those
        // endpoints carry the JWT and must be validated below. Use startsWith only
        // on the normalized path; never on the raw URI.
        boolean skipFilter =
                !bestEffortJwt &&
                !path.startsWith("/auth/profile/") &&
                (path.startsWith("/auth/") || path.equals("/order-stream") || path.startsWith("/restaurant-images/"));

        if (skipFilter) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        boolean hasBearer = header != null && header.startsWith("Bearer ");

        if (!hasBearer) {
            if (bestEffortJwt) {
                // Anonymous caller permitted for this endpoint — continue without auth.
                filterChain.doFilter(request, response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Missing or invalid token\",\"status\":401}");
            return;
        }

        String token = header.substring(7);

        if (!jwtUtil.validateToken(token)) {
            if (bestEffortJwt) {
                // Anonymous-acceptable endpoint with a bad token: ignore the token, continue.
                filterChain.doFilter(request, response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Invalid token\",\"status\":401}");
            return;
        }

        String username = jwtUtil.extractUsername(token);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}


