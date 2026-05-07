package com.kos.service;

import com.kos.controller.OrderSseController;
import com.kos.dto.Order;
import com.kos.dto.OrderFilterRequest;
import com.kos.dto.OrderFilterResponse;
import com.kos.dto.OrderItem;
import com.kos.dto.OrderStatistics;
import com.kos.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderSseController sseController;

    public OrderServiceImpl(OrderRepository orderRepository, OrderSseController sseController) {
        this.orderRepository = orderRepository;
        this.sseController = sseController;
    }

    @Override
    public List<Order> getOrdersByRestaurant(String restaurantId) {
        return orderRepository.findByRestaurantIdOrderByOrderTimeDesc(restaurantId);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    @Override
    public Order createOrder(Order order) {
        order.setId(null); // Force INSERT — client-side Date.now() IDs must not be used as DB PKs
        order.setStatus("PENDING");
        order.setOrderTime(LocalDateTime.now());

        if (order.getOrderNumber() == null || order.getOrderNumber().isBlank()) {
            order.setOrderNumber("ORD-" + System.currentTimeMillis());
        }

        if (order.getPriority() == null || order.getPriority().isBlank()) {
            order.setPriority("MEDIUM");
        }

        // Link each item back to this order; clear client-side IDs to force INSERT
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setId(null);
                item.setOrder(order);
            }
        }

        Order saved = orderRepository.save(order);
        sseController.sendOrderUpdate(saved);
        return saved;
    }

    @Override
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        sseController.sendOrderUpdate(saved);
        return saved;
    }

    @Override
    public Order updatePaymentStatus(Long orderId, String paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setPaymentStatus(paymentStatus);
        // Record payment timestamp when payment is confirmed
        if ("PAID".equals(paymentStatus) || "PARTIALLY_PAID".equals(paymentStatus)) {
            order.setPaymentDate(LocalDateTime.now());
        } else {
            order.setPaymentDate(null);
        }
        Order saved = orderRepository.save(order);
        sseController.sendOrderUpdate(saved);
        return saved;
    }

    @Override
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    @Override
    public List<Order> getOrdersByStatus(String restaurantId, String status) {
        return orderRepository.findByRestaurantIdAndStatusOrderByOrderTimeDesc(restaurantId, status);
    }

    @Override
    public List<Order> getCompletedOrders(String restaurantId) {
        return orderRepository.findByRestaurantIdAndStatusInOrderByOrderTimeDesc(
                restaurantId, Arrays.asList("SERVED", "CANCELLED"));
    }

    @Override
    public OrderFilterResponse filterOrders(OrderFilterRequest filter) {
        List<Order> allOrders;

        // Start with date range or all orders
        if (filter.getDateFrom() != null && filter.getDateTo() != null) {
            LocalDateTime from = parseDateTime(filter.getDateFrom());
            LocalDateTime to = parseDateTime(filter.getDateTo()).plusDays(1); // inclusive end date

            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                allOrders = orderRepository.findByRestaurantIdAndStatusAndOrderTimeBetweenOrderByOrderTimeDesc(
                        filter.getRestaurantId(), filter.getStatus(), from, to);
            } else {
                allOrders = orderRepository.findByRestaurantIdAndOrderTimeBetweenOrderByOrderTimeDesc(
                        filter.getRestaurantId(), from, to);
            }
        } else if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            allOrders = orderRepository.findByRestaurantIdAndStatusOrderByOrderTimeDesc(
                    filter.getRestaurantId(), filter.getStatus());
        } else {
            allOrders = orderRepository.findByRestaurantIdOrderByOrderTimeDesc(filter.getRestaurantId());
        }

        // Apply additional in-memory filters for type, paymentStatus, searchText
        if (filter.getType() != null && !filter.getType().isBlank()) {
            allOrders = allOrders.stream()
                    .filter(o -> filter.getType().equals(o.getType()))
                    .collect(Collectors.toList());
        }
        if (filter.getPaymentStatus() != null && !filter.getPaymentStatus().isBlank()) {
            allOrders = allOrders.stream()
                    .filter(o -> filter.getPaymentStatus().equals(o.getPaymentStatus()))
                    .collect(Collectors.toList());
        }
        if (filter.getSearchText() != null && !filter.getSearchText().isBlank()) {
            String search = filter.getSearchText().toLowerCase();
            allOrders = allOrders.stream()
                    .filter(o -> matchesSearch(o, search))
                    .collect(Collectors.toList());
        }

        // Paginate
        long total = allOrders.size();
        int page = filter.getPage();
        int size = filter.getSize() > 0 ? filter.getSize() : 25;
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, allOrders.size());

        List<Order> pageOrders = (fromIndex < allOrders.size())
                ? allOrders.subList(fromIndex, toIndex)
                : List.of();

        return new OrderFilterResponse(pageOrders, total, page, size);
    }

    @Override
    public OrderStatistics getStatistics(String restaurantId) {
        List<Order> allOrders = orderRepository.findByRestaurantIdOrderByOrderTimeDesc(restaurantId);

        int totalOrders = allOrders.size();
        int completedOrders = (int) allOrders.stream().filter(o -> "SERVED".equals(o.getStatus())).count();
        int cancelledOrders = (int) allOrders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count();

        double totalRevenue = allOrders.stream()
                .filter(o -> "SERVED".equals(o.getStatus()) && o.getTotalAmount() != null)
                .mapToDouble(Order::getTotalAmount)
                .sum();

        double avgOrderValue = completedOrders > 0 ? totalRevenue / completedOrders : 0.0;

        return new OrderStatistics(totalOrders, totalRevenue, avgOrderValue, completedOrders, cancelledOrders);
    }

    @Override
    public Map<String, Long> getSummaryByStatus(String restaurantId) {
        List<Order> allOrders = orderRepository.findByRestaurantIdOrderByOrderTimeDesc(restaurantId);
        Map<String, Long> summary = new LinkedHashMap<>();
        for (String status : Arrays.asList("PENDING", "PREPARING", "READY", "SERVED", "CANCELLED")) {
            summary.put(status, allOrders.stream().filter(o -> status.equals(o.getStatus())).count());
        }
        return summary;
    }

    @Override
    public Map<String, Double> getRevenueByType(String restaurantId) {
        List<Order> allOrders = orderRepository.findByRestaurantIdOrderByOrderTimeDesc(restaurantId);
        Map<String, Double> revenue = new LinkedHashMap<>();
        for (String type : Arrays.asList("DINE_IN", "TAKEAWAY", "DELIVERY")) {
            double sum = allOrders.stream()
                    .filter(o -> type.equals(o.getType()) && "SERVED".equals(o.getStatus()) && o.getTotalAmount() != null)
                    .mapToDouble(Order::getTotalAmount)
                    .sum();
            revenue.put(type, sum);
        }
        return revenue;
    }

    @Override
    public List<Order> getOrdersBySession(String sessionId) {
        return orderRepository.findBySessionIdOrderByOrderTimeAsc(sessionId);
    }

    // ── Private helpers ──

    private boolean matchesSearch(Order o, String search) {
        return (o.getOrderNumber() != null && o.getOrderNumber().toLowerCase().contains(search))
                || (o.getCustomerName() != null && o.getCustomerName().toLowerCase().contains(search))
                || (o.getTableName() != null && o.getTableName().toLowerCase().contains(search))
                || (o.getWaiterName() != null && o.getWaiterName().toLowerCase().contains(search));
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr.contains("T")) {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
    }
}
