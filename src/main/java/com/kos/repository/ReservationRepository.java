package com.kos.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kos.dto.Reservation;
import com.kos.dto.Reservation.ReservationStatus;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByRestaurantId(String restaurantId);

    List<Reservation> findByRestaurantIdAndStatus(String restaurantId, ReservationStatus status);

    // Upcoming = today or future, status is UPCOMING, CONFIRMED, or PENDING
    @Query("SELECT r FROM Reservation r WHERE r.restaurantId = :restaurantId " +
           "AND r.reservationDate >= :date " +
           "AND r.status IN ('UPCOMING', 'CONFIRMED', 'PENDING')")
    List<Reservation> findUpcoming(@Param("restaurantId") String restaurantId,
                                   @Param("date") LocalDate date);
}
