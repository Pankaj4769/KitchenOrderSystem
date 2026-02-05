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
	
	@Modifying
	@Transactional
	@Query("DELETE from ItemCategory where itemId = :itemId")
    void deleteCategoryByItemId(@Param("itemId") Integer itemId);

}

