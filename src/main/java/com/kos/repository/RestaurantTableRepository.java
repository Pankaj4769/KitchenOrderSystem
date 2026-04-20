package com.kos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kos.dto.RestaurantTable;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByRestaurantId(String restaurantId);

    List<RestaurantTable> findByRestaurantIdAndAreaName(String restaurantId, String areaName);
}
