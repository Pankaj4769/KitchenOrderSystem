package com.kos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kos.dto.AuthUser;
import com.kos.dto.UserRole;

public interface UserRepository extends JpaRepository<AuthUser, String>{

	Optional<AuthUser> findByUsername(String username);
	
	Optional<AuthUser> findByMobile(String mobile);
	
	Optional<AuthUser> findByRestaurantIdAndRole(String restaurantId, UserRole role);
	
	Optional<List<AuthUser>> findByRestaurantId(String restaurantId);
	
}
