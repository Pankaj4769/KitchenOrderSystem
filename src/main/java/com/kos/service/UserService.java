package com.kos.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kos.dto.AuthUser;
import com.kos.dto.OnboardingStatus;
import com.kos.dto.Restaurent;
import com.kos.dto.SignUpResponse;
import com.kos.dto.SignupForm;
import com.kos.dto.SubscriptionPlan;
import com.kos.dto.UserRole;
import com.kos.repository.RestaurentRepository;
import com.kos.repository.SignupRepository;
import com.kos.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	SignupRepository signupRepository;
	
	@Autowired
	RestaurentRepository rstRepository;
	
	
	public AuthUser getUserRoles(String username) {
		Optional<AuthUser> user = userRepository.findByUsername(username);
		if(user.isPresent()) {
			return user.get();
			
		}else {
			return new AuthUser();
		}
	}
	
	
	public AuthUser getUser(String username) {
		Optional<AuthUser> user = userRepository.findByUsername(username);
		if(user.isPresent()) {
			return user.get();
			
		}else {
			return new AuthUser();
		}
	}
	
	
	public Optional<AuthUser> getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public AuthUser findOrCreateByGoogle(String email, String name) {
		Optional<AuthUser> existing = userRepository.findByEmail(email);
		if (existing.isPresent()) {
			return existing.get();
		}
		AuthUser newUser = new AuthUser();
		newUser.setEmail(email);
		newUser.setName(name);
		newUser.setUsername(email);
		newUser.setRole(UserRole.OWNER);
		newUser.setFirstTime(true);
		newUser.setOnboardingStatus(OnboardingStatus.NEW);
		return userRepository.save(newUser);
	}

	public Optional<AuthUser> getUserByIdentifier(String identifier, String identifierType) {
		switch (identifierType.toLowerCase()) {
			case "email":  return userRepository.findByEmail(identifier);
			case "mobile": return userRepository.findByMobile(identifier);
			default:       return userRepository.findByUsername(identifier);
		}
	}

	public boolean updatePassword(AuthUser user) {
		try {
			userRepository.save(user);
			return true;
		}catch(Exception e) {
			return false;
		}
	}
	
	
	public SignUpResponse saveUser(SignupForm form) {
	
		SignupForm res = signupRepository.save(form);
		SignUpResponse response = new SignUpResponse();
		
		AuthUser authUser = new AuthUser();
		Restaurent restaurent = new Restaurent();
		
		
		if(res != null) {
		
			restaurent.setRestaurentName(form.getRestaurantName());
			Restaurent rest = rstRepository.save(restaurent);
			if(rest.getRestaurentId() != null) {
			
				authUser.setEmail(form.getEmail());
				authUser.setFirstTime(true);
				authUser.setMobile(form.getMobile());
				authUser.setName(form.getFullName());
				authUser.setOnboardingStatus(OnboardingStatus.NEW);
				authUser.setRestaurantId(rest.getRestaurentId().toString());
				authUser.setRole(UserRole.OWNER);
				authUser.setUsername(form.getUsername());
				authUser.setPassword(form.getPassword());
				AuthUser u = userRepository.save(authUser);
				if(u.getStaffId() != null) {
					response.setMessage("success");
					response.setStatus(true);
				}
			}
			
			
		}else {
			response.setMessage("failure");
			response.setStatus(false);
		}
		
		return response;
		
	}
	
	
}