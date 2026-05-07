package com.kos.service;

import com.kos.dto.Order;
import com.kos.dto.OrderFilterRequest;
import com.kos.dto.OrderFilterResponse;
import com.kos.dto.OrderStatistics;

import java.util.List;
import java.util.Map;

public interface OrderService {

    List<Order> getOrdersByRestaurant(String restaurantId);

    Order getOrderById(Long orderId);

    Order createOrder(Order order);

    Order updateOrderStatus(Long orderId, String status);

    Order updatePaymentStatus(Long orderId, String paymentStatus);

    void deleteOrder(Long orderId);

    List<Order> getOrdersByStatus(String restaurantId, String status);

    List<Order> getCompletedOrders(String restaurantId);

    OrderFilterResponse filterOrders(OrderFilterRequest filter);

    OrderStatistics getStatistics(String restaurantId);

    Map<String, Long> getSummaryByStatus(String restaurantId);

    Map<String, Double> getRevenueByType(String restaurantId);

    List<Order> getOrdersBySession(String sessionId);
}
