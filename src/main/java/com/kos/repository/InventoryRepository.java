package com.kos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kos.dto.Item;


@Repository
public interface InventoryRepository extends JpaRepository<Item, Integer>{

	Optional<List<Item>> findItemListByRestaurantId(String restaurantId);
	
}
