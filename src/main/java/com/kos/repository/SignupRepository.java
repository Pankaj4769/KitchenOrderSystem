package com.kos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kos.dto.SignupForm;

public interface SignupRepository extends JpaRepository<SignupForm, String>{
	
	Optional<SignupForm> findByUsername(String username);

}
