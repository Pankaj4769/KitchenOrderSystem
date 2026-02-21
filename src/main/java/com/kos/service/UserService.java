package com.kos.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kos.dto.AuthUser;
import com.kos.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	UserRepository userRepository;
	
	public AuthUser getUserRoles(String username) {
		Optional<AuthUser> user = userRepository.findById(username);
		if(user.isPresent()) {
			return user.get();
			
		}else {
			return new AuthUser();
		}
	}
	
	
}
