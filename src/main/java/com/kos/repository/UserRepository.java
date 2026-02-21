package com.kos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kos.dto.AuthUser;

public interface UserRepository extends JpaRepository<AuthUser, String>{

}
