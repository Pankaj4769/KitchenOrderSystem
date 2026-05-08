package com.kos.repository;

import com.kos.dto.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.restaurantId = :rid")
    long countTotalOrders(@Param("rid") Long restaurantId);

    @Query("""
            SELECT SUM(o.totalAmount)
            FROM Order o
            WHERE o.restaurantId = :rid
              AND o.paymentStatus = 'PAID'
        """)
        Double totalRevenue(@Param("rid") String restaurantId);
    
    @Query("""
            SELECT o.status, COUNT(o)
            FROM Order o
            WHERE o.restaurantId = :rid
            GROUP BY o.status
        """)
        List<Object[]> orderStatus(@Param("rid") String restaurantId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'SERVED' AND o.restaurantId = :rid")
    long completedOrders(@Param("rid") Long restaurantId);

    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.restaurantId = :rid GROUP BY o.status")
    List<Object[]> countByStatus(@Param("rid") Long restaurantId);

    @Query("""
        SELECT FUNCTION('HOUR', o.orderTime), SUM(o.totalAmount)
        FROM Order o
        WHERE o.restaurantId = :rid
        GROUP BY FUNCTION('HOUR', o.orderTime)
    """)
    List<Object[]> revenueByHour(@Param("rid") Long restaurantId);

    @Query("""
        SELECT o.waiterName, COUNT(o), SUM(o.totalAmount)
        FROM Order o
        WHERE o.restaurantId = :rid
        GROUP BY o.waiterName
    """)
    List<Object[]> waiterStats(@Param("rid") Long restaurantId);

    // --- SALES ANALYTICS QUERIES ---

    @Query("SELECT COUNT(o) FROM Order o WHERE o.restaurantId = :rid AND o.orderTime BETWEEN :start AND :end")
    long countTotalOrdersByDate(@Param("rid") String restaurantId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.restaurantId = :rid AND o.status = 'SERVED' AND o.orderTime BETWEEN :start AND :end")
    long countCompletedOrdersByDate(@Param("rid") String restaurantId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.restaurantId = :rid AND o.status = 'CANCELLED' AND o.orderTime BETWEEN :start AND :end")
    long countCancelledOrdersByDate(@Param("rid") String restaurantId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.restaurantId = :rid AND o.status = 'SERVED' AND o.orderTime BETWEEN :start AND :end")
    Double calculateGrossSalesByDate(@Param("rid") String restaurantId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.restaurantId = :rid AND o.status = 'SERVED' AND o.paymentStatus = 'PAID' AND o.orderTime BETWEEN :start AND :end")
    Double calculateNetSalesByDate(@Param("rid") String restaurantId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o.type, COUNT(o), SUM(o.totalAmount) FROM Order o WHERE o.restaurantId = :rid AND o.status = 'SERVED' AND o.orderTime BETWEEN :start AND :end GROUP BY o.type")
    List<Object[]> getOrderTypeBreakdownByDate(@Param("rid") String restaurantId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o.waiterName, COUNT(o), SUM(o.totalAmount) FROM Order o WHERE o.restaurantId = :rid AND o.status = 'SERVED' AND o.orderTime BETWEEN :start AND :end GROUP BY o.waiterName")
    List<Object[]> getWaiterStatsByDate(@Param("rid") String restaurantId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('HOUR', o.orderTime), SUM(o.totalAmount) FROM Order o WHERE o.restaurantId = :rid AND o.status = 'SERVED' AND o.orderTime BETWEEN :start AND :end GROUP BY FUNCTION('HOUR', o.orderTime)")
    List<Object[]> getRevenueByHourByDate(@Param("rid") String restaurantId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
