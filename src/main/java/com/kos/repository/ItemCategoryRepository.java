package com.kos.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kos.dto.ItemCategory;


@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Integer>{
	
	@Query("SELECT categoryType FROM ItemCategory WHERE itemId = :itemId")
    List<String> findCategoryByItemId(@Param("itemId") Integer itemId);

}

