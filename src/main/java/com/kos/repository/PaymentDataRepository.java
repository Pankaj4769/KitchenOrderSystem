package com.kos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kos.dto.PaymentData;

import java.util.Optional;
import java.util.List;

@Repository
public interface PaymentDataRepository extends JpaRepository<PaymentData, Long> {

    Optional<PaymentData> findByTransactionId(String transactionId);

    List<PaymentData> findByRestaurantId(String restaurantId);

    @org.springframework.data.jpa.repository.Query("SELECT p.method, COUNT(p), SUM(p.amount) FROM PaymentData p WHERE p.restaurantId = :rid AND p.timestamp BETWEEN :start AND :end GROUP BY p.method")
    List<Object[]> getPaymentBreakdownByDate(@org.springframework.data.repository.query.Param("rid") String restaurantId, @org.springframework.data.repository.query.Param("start") java.time.Instant start, @org.springframework.data.repository.query.Param("end") java.time.Instant end);
}
