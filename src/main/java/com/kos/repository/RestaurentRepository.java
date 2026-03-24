package com.kos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kos.dto.Restaurent;

public interface RestaurentRepository extends JpaRepository<Restaurent, Integer>{
	
	Optional<Restaurent> findByRestaurentName(String restaurentName);

}
