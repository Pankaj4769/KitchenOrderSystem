package com.kos.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.kos.dto.AuthUser;
import com.kos.dto.OnboardingStatus;
import com.kos.dto.Restaurent;
import com.kos.dto.SignUpResponse;
import com.kos.dto.SignupForm;
import com.kos.dto.SubscriptionPlan;
import com.kos.dto.UserRole;
import com.kos.model.Subscription.SubscriptionStatus;
import com.kos.repository.RestaurentRepository;
import com.kos.repository.SignupRepository;
import com.kos.repository.SubscriptionRepository;
import com.kos.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	SignupRepository signupRepository;

	@Autowired
	RestaurentRepository rstRepository;

	@Autowired
	SubscriptionRepository subscriptionRepository;

	@Autowired
	@Nullable
	private JavaMailSender mailSender;
	
	
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

	public Optional<AuthUser> getOwnerByRestaurantId(String restaurantId) {
		return userRepository.findByRestaurantIdAndRole(restaurantId, UserRole.OWNER);
	}

	public List<AuthUser> getStaffByRestaurant(String restaurantId) {
		return userRepository.findByRestaurantId(restaurantId)
				.orElse(Collections.emptyList())
				.stream()
				.filter(u -> u.getRole() != UserRole.OWNER)
				.collect(Collectors.toList());
	}

	public AuthUser createStaff(AuthUser request) {
		// Auto-set username to mobile if not provided
		if (request.getUsername() == null || request.getUsername().isBlank()) {
			request.setUsername(request.getMobile());
		}
		// Generate temp password if not provided
		String tempPassword = (request.getPassword() == null || request.getPassword().isBlank())
				? generateTempPassword()
				: request.getPassword();
		request.setPassword(tempPassword);
		request.setFirstTime(false);
		request.setMustResetPassword(true);

		// Inherit onboarding status and subscription plan from the owner of the same restaurant
		Optional.ofNullable(request.getRestaurantId())
				.filter(id -> !id.isBlank())
				.flatMap(id -> userRepository.findByRestaurantIdAndRole(id, UserRole.OWNER))
				.ifPresentOrElse(owner -> {
					request.setOnboardingStatus(owner.getOnboardingStatus() != null
							? owner.getOnboardingStatus() : OnboardingStatus.COMPLETED);
					// Use plan stored on owner record; fall back to active subscription table
					SubscriptionPlan plan = owner.getSubscriptionPlan();
					if (plan == null) {
						try {
							Long restaurantIdLong = Long.parseLong(request.getRestaurantId());
							plan = subscriptionRepository
									.findByRestaurantIdAndStatus(restaurantIdLong, SubscriptionStatus.ACTIVE)
									.map(s -> SubscriptionPlan.valueOf(s.getPlan().getPlanName().name()))
									.orElse(null);
						} catch (Exception ignored) {}
					}
					request.setSubscriptionPlan(plan);
				}, () -> request.setOnboardingStatus(OnboardingStatus.COMPLETED));
		AuthUser saved = userRepository.save(request);
		// Send credentials to staff via email
		sendStaffCredentials(saved.getEmail(), saved.getName(), saved.getUsername(), tempPassword);
		return saved;
	}

	public boolean resendTempPassword(String username) {
		Optional<AuthUser> optUser = userRepository.findByUsername(username);
		if (optUser.isEmpty()) return false;
		AuthUser user = optUser.get();
		String tempPassword = generateTempPassword();
		user.setPassword(tempPassword);
		user.setMustResetPassword(true);
		userRepository.save(user);
		return sendStaffCredentials(user.getEmail(), user.getName(), user.getUsername(), tempPassword);
	}

	public Optional<AuthUser> resetTempPassword(String username, String newPassword) {
		Optional<AuthUser> optUser = userRepository.findByUsername(username);
		if (optUser.isEmpty()) return Optional.empty();
		AuthUser user = optUser.get();
		user.setPassword(newPassword);
		user.setMustResetPassword(false);
		return Optional.of(userRepository.save(user));
	}

	public String generateTempPassword() {
		String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
		Random random = new Random();
		StringBuilder sb = new StringBuilder(8);
		for (int i = 0; i < 8; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}

	public boolean sendStaffCredentials(String toEmail, String name, String username, String tempPassword) {
		if (toEmail == null || toEmail.isBlank()) return false;
		if (mailSender == null) {
			System.err.println("JavaMailSender not configured — skipping email to " + toEmail);
			return false;
		}
		try {
			SimpleMailMessage msg = new SimpleMailMessage();
			msg.setTo(toEmail);
			msg.setSubject("Welcome to KOS – Your Login Credentials");
			msg.setText(
				"Hello " + (name != null ? name : "there") + ",\n\n" +
				"Your KOS account has been created. Here are your login details:\n\n" +
				"Username: " + username + "\n" +
				"Temporary Password: " + tempPassword + "\n\n" +
				"Please log in and change your password on first use.\n\n" +
				"Best regards,\nKOS Team"
			);
			mailSender.send(msg);
			return true;
		} catch (Exception e) {
			System.err.println("Failed to send staff credentials email: " + e.getMessage());
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