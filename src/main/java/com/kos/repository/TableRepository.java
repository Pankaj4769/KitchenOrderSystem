package com.kos.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.kos.dto.RestaurantTable;

import java.util.List;

public interface TableRepository extends JpaRepository<RestaurantTable, Long> {

//    @Query("SELECT t.status, COUNT(t) FROM TableEntity t WHERE t.restaurantId = :rid GROUP BY t.status")
//    List<Object[]> tableStatus(@Param("rid") Long restaurantId);
}
