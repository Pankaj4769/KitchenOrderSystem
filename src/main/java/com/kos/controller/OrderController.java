package com.kos.controller;

import com.kos.dto.Order;
import com.kos.dto.OrderFilterRequest;
import com.kos.dto.OrderFilterResponse;
import com.kos.dto.OrderStatistics;
import com.kos.service.OrderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LogManager.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * GET /orders?restaurantId=X
     * Returns all orders for the given restaurant, newest first.
     */
    @GetMapping
    public ResponseEntity<List<Order>> getOrders(@RequestParam String restaurantId) {
        logger.info("Entering getOrders() with restaurantId={}", restaurantId);
        try {
            ResponseEntity<List<Order>> result = ResponseEntity.ok(orderService.getOrdersByRestaurant(restaurantId));
            logger.info("Exiting getOrders()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getOrders(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * GET /orders/{id}
     * Returns a single order by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        logger.info("Entering getOrderById() with id={}", id);
        try {
            ResponseEntity<Order> result = ResponseEntity.ok(orderService.getOrderById(id));
            logger.info("Exiting getOrderById()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getOrderById(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * GET /orders/history?restaurantId=X
     * Returns completed orders (SERVED + CANCELLED) for the given restaurant.
     */
    @GetMapping("/history")
    public ResponseEntity<List<Order>> getOrderHistory(@RequestParam String restaurantId) {
        logger.info("Entering getOrderHistory() with restaurantId={}", restaurantId);
        try {
            ResponseEntity<List<Order>> result = ResponseEntity.ok(orderService.getCompletedOrders(restaurantId));
            logger.info("Exiting getOrderHistory()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getOrderHistory(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * GET /orders/by-status?restaurantId=X&status=SERVED
     * Returns orders filtered by a single status.
     */
    @GetMapping("/by-status")
    public ResponseEntity<List<Order>> getOrdersByStatus(
            @RequestParam String restaurantId,
            @RequestParam String status) {
        logger.info("Entering getOrdersByStatus() with restaurantId={}", restaurantId);
        try {
            ResponseEntity<List<Order>> result = ResponseEntity.ok(orderService.getOrdersByStatus(restaurantId, status));
            logger.info("Exiting getOrdersByStatus()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getOrdersByStatus(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * POST /orders/filter
     * Server-side filtering with pagination.
     * Body: { restaurantId, status, type, paymentStatus, dateFrom, dateTo, searchText, page, size }
     */
    @PostMapping("/filter")
    public ResponseEntity<OrderFilterResponse> filterOrders(@RequestBody OrderFilterRequest filter) {
        logger.info("Entering filterOrders()");
        try {
            ResponseEntity<OrderFilterResponse> result = ResponseEntity.ok(orderService.filterOrders(filter));
            logger.info("Exiting filterOrders()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in filterOrders(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * GET /orders/stats?restaurantId=X
     * Returns aggregated statistics for a restaurant.
     */
    @GetMapping("/stats")
    public ResponseEntity<OrderStatistics> getStatistics(@RequestParam String restaurantId) {
        logger.info("Entering getStatistics() with restaurantId={}", restaurantId);
        try {
            ResponseEntity<OrderStatistics> result = ResponseEntity.ok(orderService.getStatistics(restaurantId));
            logger.info("Exiting getStatistics()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getStatistics(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * GET /orders/summary-by-status?restaurantId=X
     * Returns order count grouped by status.
     */
    @GetMapping("/summary-by-status")
    public ResponseEntity<Map<String, Long>> getSummaryByStatus(@RequestParam String restaurantId) {
        logger.info("Entering getSummaryByStatus() with restaurantId={}", restaurantId);
        try {
            ResponseEntity<Map<String, Long>> result = ResponseEntity.ok(orderService.getSummaryByStatus(restaurantId));
            logger.info("Exiting getSummaryByStatus()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getSummaryByStatus(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * GET /orders/revenue-by-type?restaurantId=X
     * Returns revenue grouped by order type (DINE_IN, TAKEAWAY, DELIVERY).
     */
    @GetMapping("/revenue-by-type")
    public ResponseEntity<Map<String, Double>> getRevenueByType(@RequestParam String restaurantId) {
        logger.info("Entering getRevenueByType() with restaurantId={}", restaurantId);
        try {
            ResponseEntity<Map<String, Double>> result = ResponseEntity.ok(orderService.getRevenueByType(restaurantId));
            logger.info("Exiting getRevenueByType()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getRevenueByType(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * POST /orders
     * Creates a new order. Backend sets status=PENDING, orderTime=now, generates orderNumber.
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        logger.info("Entering createOrder()");
        try {
            ResponseEntity<Order> result = ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(order));
            logger.info("Exiting createOrder()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in createOrder(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * PATCH /orders/{id}/status
     * Body: { "status": "PREPARING" }
     * Valid values: PENDING, PREPARING, READY, SERVED, CANCELLED
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        logger.info("Entering updateStatus() with id={}", id);
        try {
            String status = body.get("status");
            ResponseEntity<Order> result = ResponseEntity.ok(orderService.updateOrderStatus(id, status));
            logger.info("Exiting updateStatus()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in updateStatus(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * PATCH /orders/{id}/payment
     * Body: { "paymentStatus": "PAID" }
     * Valid values: PENDING, PAID, PARTIALLY_PAID, REFUNDED
     * Sets paymentDate to now when status is PAID or PARTIALLY_PAID.
     */
    @PatchMapping("/{id}/payment")
    public ResponseEntity<Order> updatePayment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        logger.info("Entering updatePayment() with id={}", id);
        try {
            String paymentStatus = body.get("paymentStatus");
            ResponseEntity<Order> result = ResponseEntity.ok(orderService.updatePaymentStatus(id, paymentStatus));
            logger.info("Exiting updatePayment()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in updatePayment(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * GET /orders/by-session?sessionId=SES-3-xxx
     * Returns all orders for a session in chronological order (all KOT rounds).
     * Used by cashier billing to aggregate every device's orders for one table.
     */
    @GetMapping("/by-session")
    public ResponseEntity<List<Order>> getOrdersBySession(@RequestParam String sessionId) {
        logger.info("Entering getOrdersBySession()");
        try {
            ResponseEntity<List<Order>> result = ResponseEntity.ok(orderService.getOrdersBySession(sessionId));
            logger.info("Exiting getOrdersBySession()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getOrdersBySession(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * DELETE /orders/{id}
     * Permanently removes an order and its items.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        logger.info("Entering deleteOrder() with id={}", id);
        try {
            orderService.deleteOrder(id);
            logger.info("Exiting deleteOrder()");
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error in deleteOrder(): {}", e.getMessage(), e);
            throw e;
        }
    }
}
