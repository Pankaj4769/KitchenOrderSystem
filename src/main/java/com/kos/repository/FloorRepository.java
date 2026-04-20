package com.kos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kos.dto.Floor;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Long> {

    List<Floor> findByRestaurantId(String restaurantId);
}
