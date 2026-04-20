package com.kos.repository;

import com.kos.dto.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByRestaurantIdOrderByOrderTimeDesc(String restaurantId);

    List<Order> findByRestaurantIdAndStatusOrderByOrderTimeDesc(String restaurantId, String status);

    List<Order> findByRestaurantIdAndStatusInOrderByOrderTimeDesc(String restaurantId, List<String> statuses);

    List<Order> findByRestaurantIdAndOrderTimeBetweenOrderByOrderTimeDesc(String restaurantId, LocalDateTime start, LocalDateTime end);

    List<Order> findByRestaurantIdAndStatusAndOrderTimeBetweenOrderByOrderTimeDesc(
            String restaurantId, String status, LocalDateTime start, LocalDateTime end);

    List<Order> findBySessionIdOrderByOrderTimeAsc(String sessionId);
}
