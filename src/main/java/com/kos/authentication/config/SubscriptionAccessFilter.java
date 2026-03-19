package com.kos.authentication.config;

import com.kos.exception.FeatureNotAllowedException;
import com.kos.exception.SubscriptionExpiredException;
import com.kos.service.SubscriptionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SubscriptionAccessFilter {

//	private final SubscriptionService subscriptionService;
//
//	@Autowired
//	public SubscriptionAccessFilter(SubscriptionService subscriptionService) {
//	    this.subscriptionService = subscriptionService;
//	}
//
//    // Map URI patterns to required features
//    private static final Map<String, String> FEATURE_MAP = Map.of(
//        "/api/inventory",   "INVENTORY_MGMT",
//        "/api/billing",     "BILLING",
//        "/api/kitchen",     "BASIC_KITCHEN_VIEW",
//        "/api/orders",      "BASIC_ORDER_MGMT",
//        "/api/orders/sse",  "SSE_LIVE_ORDERS"
//    );
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//
//        String uri = request.getRequestURI();
//        String restaurantIdHeader = request.getHeader("X-Restaurant-Id");
//
//        // Skip filter for auth and subscription endpoints
//        if (uri.startsWith("/api/auth") || uri.startsWith("/api/subscription")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        if (restaurantIdHeader == null) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        try {
//            Long restaurantId = Long.parseLong(restaurantIdHeader);
//            for (Map.Entry<String, String> entry : FEATURE_MAP.entrySet()) {
//                if (uri.startsWith(entry.getKey())) {
//                    subscriptionService.hasFeatureAccess(restaurantId, entry.getValue());
//                    break;
//                }
//            }
//            filterChain.doFilter(request, response);
//
//        } catch (SubscriptionExpiredException | FeatureNotAllowedException ex) {
//            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//            response.setContentType("application/json");
//            response.getWriter().write(
//                "{\"error\": \"" + ex.getMessage() + "\"}"
//            );
//        }
//    }
}
