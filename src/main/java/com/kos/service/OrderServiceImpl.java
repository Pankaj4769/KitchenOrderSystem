package com.kos.service;

import com.kos.controller.OrderSseController;
import com.kos.dto.Order;
import com.kos.dto.OrderFilterRequest;
import com.kos.dto.OrderFilterResponse;
import com.kos.dto.OrderItem;
import com.kos.dto.OrderStatistics;
import com.kos.repository.OrderRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderSseController sseController;

    public OrderServiceImpl(OrderRepository orderRepository, OrderSseController sseController) {
        this.orderRepository = orderRepository;
        this.sseController = sseController;
    }

    @Override
    public List<Order> getOrdersByRestaurant(String restaurantId) {
        logger.info("Entering getOrdersByRestaurant() with restaurantId={}", restaurantId);
        try {
            List<Order> result = orderRepository.findByRestaurantIdOrderByOrderTimeDesc(restaurantId);
            logger.info("Exiting getOrdersByRestaurant()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getOrdersByRestaurant(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Order getOrderById(Long orderId) {
        logger.info("Entering getOrderById() with orderId={}", orderId);
        try {
            Order result = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            logger.info("Exiting getOrderById()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getOrderById(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Order createOrder(Order order) {
        logger.info("Entering createOrder()");
        try {
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
            logger.info("Exiting createOrder()");
            return saved;
        } catch (RuntimeException e) {
            logger.error("Error in createOrder(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Order updateOrderStatus(Long orderId, String status) {
        logger.info("Entering updateOrderStatus() with orderId={}", orderId);
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            order.setStatus(status);
            Order saved = orderRepository.save(order);
            sseController.sendOrderUpdate(saved);
            logger.info("Exiting updateOrderStatus()");
            return saved;
        } catch (RuntimeException e) {
            logger.error("Error in updateOrderStatus(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Order updatePaymentStatus(Long orderId, String paymentStatus) {
        logger.info("Entering updatePaymentStatus() with orderId={}", orderId);
        try {
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
            logger.info("Exiting updatePaymentStatus()");
            return saved;
        } catch (RuntimeException e) {
            logger.error("Error in updatePaymentStatus(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void deleteOrder(Long orderId) {
        logger.info("Entering deleteOrder() with orderId={}", orderId);
        try {
            orderRepository.deleteById(orderId);
            logger.info("Exiting deleteOrder()");
        } catch (RuntimeException e) {
            logger.error("Error in deleteOrder(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<Order> getOrdersByStatus(String restaurantId, String status) {
        logger.info("Entering getOrdersByStatus() with restaurantId={}", restaurantId);
        try {
            List<Order> result = orderRepository.findByRestaurantIdAndStatusOrderByOrderTimeDesc(restaurantId, status);
            logger.info("Exiting getOrdersByStatus()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getOrdersByStatus(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<Order> getCompletedOrders(String restaurantId) {
        logger.info("Entering getCompletedOrders() with restaurantId={}", restaurantId);
        try {
            List<Order> result = orderRepository.findByRestaurantIdAndStatusInOrderByOrderTimeDesc(
                    restaurantId, Arrays.asList("SERVED", "CANCELLED"));
            logger.info("Exiting getCompletedOrders()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getCompletedOrders(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public OrderFilterResponse filterOrders(OrderFilterRequest filter) {
        logger.info("Entering filterOrders()");
        try {
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

            OrderFilterResponse result = new OrderFilterResponse(pageOrders, total, page, size);
            logger.info("Exiting filterOrders()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in filterOrders(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public OrderStatistics getStatistics(String restaurantId) {
        logger.info("Entering getStatistics() with restaurantId={}", restaurantId);
        try {
            List<Order> allOrders = orderRepository.findByRestaurantIdOrderByOrderTimeDesc(restaurantId);

            int totalOrders = allOrders.size();
            int completedOrders = (int) allOrders.stream().filter(o -> "SERVED".equals(o.getStatus())).count();
            int cancelledOrders = (int) allOrders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count();

            double totalRevenue = allOrders.stream()
                    .filter(o -> "SERVED".equals(o.getStatus()) && o.getTotalAmount() != null)
                    .mapToDouble(Order::getTotalAmount)
                    .sum();

            double avgOrderValue = completedOrders > 0 ? totalRevenue / completedOrders : 0.0;

            OrderStatistics result = new OrderStatistics(totalOrders, totalRevenue, avgOrderValue, completedOrders, cancelledOrders);
            logger.info("Exiting getStatistics()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getStatistics(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Map<String, Long> getSummaryByStatus(String restaurantId) {
        logger.info("Entering getSummaryByStatus() with restaurantId={}", restaurantId);
        try {
            List<Order> allOrders = orderRepository.findByRestaurantIdOrderByOrderTimeDesc(restaurantId);
            Map<String, Long> summary = new LinkedHashMap<>();
            for (String status : Arrays.asList("PENDING", "PREPARING", "READY", "SERVED", "CANCELLED")) {
                summary.put(status, allOrders.stream().filter(o -> status.equals(o.getStatus())).count());
            }
            logger.info("Exiting getSummaryByStatus()");
            return summary;
        } catch (RuntimeException e) {
            logger.error("Error in getSummaryByStatus(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Map<String, Double> getRevenueByType(String restaurantId) {
        logger.info("Entering getRevenueByType() with restaurantId={}", restaurantId);
        try {
            List<Order> allOrders = orderRepository.findByRestaurantIdOrderByOrderTimeDesc(restaurantId);
            Map<String, Double> revenue = new LinkedHashMap<>();
            for (String type : Arrays.asList("DINE_IN", "TAKEAWAY", "DELIVERY")) {
                double sum = allOrders.stream()
                        .filter(o -> type.equals(o.getType()) && "SERVED".equals(o.getStatus()) && o.getTotalAmount() != null)
                        .mapToDouble(Order::getTotalAmount)
                        .sum();
                revenue.put(type, sum);
            }
            logger.info("Exiting getRevenueByType()");
            return revenue;
        } catch (RuntimeException e) {
            logger.error("Error in getRevenueByType(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<Order> getOrdersBySession(String sessionId) {
        logger.info("Entering getOrdersBySession()");
        try {
            List<Order> result = orderRepository.findBySessionIdOrderByOrderTimeAsc(sessionId);
            logger.info("Exiting getOrdersBySession()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getOrdersBySession(): {}", e.getMessage(), e);
            throw e;
        }
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
