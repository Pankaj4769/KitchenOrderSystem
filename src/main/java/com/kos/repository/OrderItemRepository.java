package com.kos.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.kos.dto.OrderItem;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	 // ✅ Category Stats
    @Query("""
        SELECT i.category,
               COUNT(i),
               SUM(i.quantity * i.price)
        FROM OrderItem i
        WHERE i.order.restaurantId = :rid
        GROUP BY i.category
    """)
    List<Object[]> categoryStats(@Param("rid") String restaurantId);


    // ✅ Top Selling Items
    @Query("""
        SELECT i.name, i.category,
               SUM(i.quantity),
               SUM(i.quantity * i.price)
        FROM OrderItem i
        WHERE i.order.restaurantId = :rid
        GROUP BY i.name, i.category
        ORDER BY SUM(i.quantity) DESC
    """)
    List<Object[]> topSelling(@Param("rid") String restaurantId);

    // --- SALES ANALYTICS QUERIES ---

    @Query("""
        SELECT i.name, i.category,
               SUM(i.quantity),
               SUM(i.quantity * i.price)
        FROM OrderItem i
        WHERE i.order.restaurantId = :rid
          AND i.order.status = 'SERVED'
          AND i.order.orderTime BETWEEN :start AND :end
        GROUP BY i.name, i.category
        ORDER BY SUM(i.quantity) DESC
    """)
    List<Object[]> getTopSellingItemsByDate(@Param("rid") String restaurantId, @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}
