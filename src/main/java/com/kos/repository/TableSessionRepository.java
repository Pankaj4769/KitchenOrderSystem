package com.kos.repository;

import com.kos.dto.TableSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TableSessionRepository extends JpaRepository<TableSession, String> {

    /** Find the single active session for a table (there should be at most one). */
    Optional<TableSession> findByTableIdAndRestaurantIdAndStatus(
            Long tableId, String restaurantId, String status);

    /** All sessions for a restaurant that are currently active or awaiting billing. */
    List<TableSession> findByRestaurantIdAndStatusIn(String restaurantId, List<String> statuses);
}
