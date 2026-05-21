package com.kos.authentication.config;

import com.kos.exception.FeatureNotAllowedException;
import com.kos.exception.SubscriptionExpiredException;
import com.kos.model.Subscription;
import com.kos.model.Subscription.SubscriptionStatus;
import com.kos.repository.SubscriptionRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubscriptionAccessFilter extends OncePerRequestFilter {

	@Autowired
    private SubscriptionService subscriptionService;
	@Autowired
    private SubscriptionRepository subscriptionRepository;

    // Map URI patterns to required features
    private static final Map<String, String> FEATURE_MAP = Map.of(
        "/api/inventory",   "INVENTORY_MGMT",
        "/api/billing",     "BILLING",
        "/api/kitchen",     "BASIC_KITCHEN_VIEW",
        "/api/orders",      "BASIC_ORDER_MGMT",
        "/api/orders/sse",  "SSE_LIVE_ORDERS"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String restaurantIdHeader = request.getHeader("X-Restaurant-Id");

        // Skip filter for auth, subscription, payment webhook, login, signup endpoints.
        // /doPayment is also skipped — it's the recovery endpoint a user with an
        // EXPIRED subscription or ended trial calls to renew, so blocking it would
        // make recovery impossible.
        if (uri.startsWith("/api/auth")
                || uri.startsWith("/api/subscription")
                || uri.startsWith("/api/payment/webhook")
                || uri.startsWith("/login")
                || uri.startsWith("/signup")
                || uri.startsWith("/auth/")
                || uri.equals("/doPayment")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (restaurantIdHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Long restaurantId = Long.parseLong(restaurantIdHeader);

            // Check subscription status directly for EXPIRED / trial-expired → 402
            Optional<Subscription> subOpt = subscriptionRepository
                    .findByRestaurantIdAndStatusIn(restaurantId,
                            List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL));

            if (subOpt.isPresent()) {
                Subscription subscription = subOpt.get();

                // EXPIRED check
                boolean isExpired = SubscriptionStatus.EXPIRED.equals(subscription.getStatus());

                // TRIAL same-day expiry check
                boolean isTrial = SubscriptionStatus.TRIAL.equals(subscription.getStatus());
                boolean trialExpired = isTrial && subscription.getTrialEndDate() != null
                        && subscription.getTrialEndDate().isBefore(LocalDate.now());

                if (isExpired || trialExpired) {
                    response.setStatus(402); // Payment Required
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Subscription expired or trial ended. Please renew your subscription.\",\"code\":\"SUBSCRIPTION_EXPIRED\"}");
                    return;
                }
            }

            // Feature-level access check
            for (Map.Entry<String, String> entry : FEATURE_MAP.entrySet()) {
                if (uri.startsWith(entry.getKey())) {
                    subscriptionService.hasFeatureAccess(restaurantId, entry.getValue());
                    break;
                }
            }

            filterChain.doFilter(request, response);

        } catch (SubscriptionExpiredException ex) {
            response.setStatus(402); // Payment Required
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Subscription expired or trial ended. Please renew your subscription.\",\"code\":\"SUBSCRIPTION_EXPIRED\"}");
        } catch (FeatureNotAllowedException ex) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\": \"" + ex.getMessage() + "\"}"
            );
        }
    }
}
