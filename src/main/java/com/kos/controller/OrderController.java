package com.kos.controller;

import com.kos.dto.Order;
import com.kos.dto.OrderFilterRequest;
import com.kos.dto.OrderFilterResponse;
import com.kos.dto.OrderStatistics;
import com.kos.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

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
        return ResponseEntity.ok(orderService.getOrdersByRestaurant(restaurantId));
    }

    /**
     * GET /orders/{id}
     * Returns a single order by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * GET /orders/history?restaurantId=X
     * Returns completed orders (SERVED + CANCELLED) for the given restaurant.
     */
    @GetMapping("/history")
    public ResponseEntity<List<Order>> getOrderHistory(@RequestParam String restaurantId) {
        return ResponseEntity.ok(orderService.getCompletedOrders(restaurantId));
    }

    /**
     * GET /orders/by-status?restaurantId=X&status=SERVED
     * Returns orders filtered by a single status.
     */
    @GetMapping("/by-status")
    public ResponseEntity<List<Order>> getOrdersByStatus(
            @RequestParam String restaurantId,
            @RequestParam String status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(restaurantId, status));
    }

    /**
     * POST /orders/filter
     * Server-side filtering with pagination.
     * Body: { restaurantId, status, type, paymentStatus, dateFrom, dateTo, searchText, page, size }
     */
    @PostMapping("/filter")
    public ResponseEntity<OrderFilterResponse> filterOrders(@RequestBody OrderFilterRequest filter) {
        return ResponseEntity.ok(orderService.filterOrders(filter));
    }

    /**
     * GET /orders/stats?restaurantId=X
     * Returns aggregated statistics for a restaurant.
     */
    @GetMapping("/stats")
    public ResponseEntity<OrderStatistics> getStatistics(@RequestParam String restaurantId) {
        return ResponseEntity.ok(orderService.getStatistics(restaurantId));
    }

    /**
     * GET /orders/summary-by-status?restaurantId=X
     * Returns order count grouped by status.
     */
    @GetMapping("/summary-by-status")
    public ResponseEntity<Map<String, Long>> getSummaryByStatus(@RequestParam String restaurantId) {
        return ResponseEntity.ok(orderService.getSummaryByStatus(restaurantId));
    }

    /**
     * GET /orders/revenue-by-type?restaurantId=X
     * Returns revenue grouped by order type (DINE_IN, TAKEAWAY, DELIVERY).
     */
    @GetMapping("/revenue-by-type")
    public ResponseEntity<Map<String, Double>> getRevenueByType(@RequestParam String restaurantId) {
        return ResponseEntity.ok(orderService.getRevenueByType(restaurantId));
    }

    /**
     * POST /orders
     * Creates a new order. Backend sets status=PENDING, orderTime=now, generates orderNumber.
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(order));
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
        String status = body.get("status");
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
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
        String paymentStatus = body.get("paymentStatus");
        return ResponseEntity.ok(orderService.updatePaymentStatus(id, paymentStatus));
    }

    /**
     * GET /orders/by-session?sessionId=SES-3-xxx
     * Returns all orders for a session in chronological order (all KOT rounds).
     * Used by cashier billing to aggregate every device's orders for one table.
     */
    @GetMapping("/by-session")
    public ResponseEntity<List<Order>> getOrdersBySession(@RequestParam String sessionId) {
        return ResponseEntity.ok(orderService.getOrdersBySession(sessionId));
    }

    /**
     * DELETE /orders/{id}
     * Permanently removes an order and its items.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
