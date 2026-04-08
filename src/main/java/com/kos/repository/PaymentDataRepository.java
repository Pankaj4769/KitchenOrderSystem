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
}
