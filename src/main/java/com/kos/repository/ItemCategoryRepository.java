package com.kos.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kos.dto.ItemCategory;

import jakarta.transaction.Transactional;


@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Integer>{
	
	@Query("SELECT categoryType FROM ItemCategory WHERE itemId = :itemId")
    List<String> findCategoryByItemId(@Param("itemId") Integer itemId);

	@Query("SELECT DISTINCT ic.categoryType FROM ItemCategory ic " +
	       "WHERE ic.itemId IN (SELECT i.itemId FROM Item i WHERE i.restaurantId = :restId)")
    List<String> findDistinctCategoriesByRestaurantId(@Param("restId") String restId);
	
	@Modifying
	@Transactional
	@Query("DELETE from ItemCategory where itemId = :itemId")
    void deleteCategoryByItemId(@Param("itemId") Integer itemId);

}

