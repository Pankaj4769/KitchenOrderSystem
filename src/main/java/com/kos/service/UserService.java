package com.kos.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

	private static final Logger logger = LogManager.getLogger(UserService.class);

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
		logger.info("Entering getUserRoles()");
		try {
			Optional<AuthUser> user = userRepository.findByUsername(username);
			if(user.isPresent()) {
				logger.info("Exiting getUserRoles()");
				return user.get();
			}else {
				logger.info("Exiting getUserRoles()");
				return new AuthUser();
			}
		} catch (RuntimeException e) {
			logger.error("Error in getUserRoles(): {}", e.getMessage(), e);
			throw e;
		}
	}


	public AuthUser getUser(String username) {
		logger.info("Entering getUser()");
		try {
			Optional<AuthUser> user = userRepository.findByUsername(username);
			if(user.isPresent()) {
				logger.info("Exiting getUser()");
				return user.get();

			}else {
				logger.info("Exiting getUser()");
				return new AuthUser();
			}
		} catch (RuntimeException e) {
			logger.error("Error in getUser(): {}", e.getMessage(), e);
			throw e;
		}
	}


	public Optional<AuthUser> getUserByEmail(String email) {
		logger.info("Entering getUserByEmail()");
		try {
			Optional<AuthUser> result = userRepository.findByEmail(email);
			logger.info("Exiting getUserByEmail()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in getUserByEmail(): {}", e.getMessage(), e);
			throw e;
		}
	}

	public AuthUser findOrCreateByGoogle(String email, String name) {
		logger.info("Entering findOrCreateByGoogle()");
		try {
			Optional<AuthUser> existing = userRepository.findByEmail(email);
			if (existing.isPresent()) {
				logger.info("Exiting findOrCreateByGoogle()");
				return existing.get();
			}
			AuthUser newUser = new AuthUser();
			newUser.setEmail(email);
			newUser.setName(name);
			newUser.setUsername(email);
			newUser.setRole(UserRole.OWNER);
			newUser.setFirstTime(true);
			newUser.setOnboardingStatus(OnboardingStatus.NEW);
			AuthUser result = userRepository.save(newUser);
			logger.info("Exiting findOrCreateByGoogle()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in findOrCreateByGoogle(): {}", e.getMessage(), e);
			throw e;
		}
	}

	public Optional<AuthUser> getUserByIdentifier(String identifier, String identifierType) {
		logger.info("Entering getUserByIdentifier()");
		try {
			Optional<AuthUser> result;
			switch (identifierType.toLowerCase()) {
				case "email":  result = userRepository.findByEmail(identifier); break;
				case "mobile": result = userRepository.findByMobile(identifier); break;
				default:       result = userRepository.findByUsername(identifier); break;
			}
			logger.info("Exiting getUserByIdentifier()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in getUserByIdentifier(): {}", e.getMessage(), e);
			throw e;
		}
	}

	public boolean updatePassword(AuthUser user) {
		logger.info("Entering updatePassword()");
		try {
			try {
				userRepository.save(user);
				logger.info("Exiting updatePassword()");
				return true;
			}catch(Exception e) {
				logger.error("Error during updatePassword save: {}", e.getMessage(), e);
				logger.info("Exiting updatePassword()");
				return false;
			}
		} catch (RuntimeException e) {
			logger.error("Error in updatePassword(): {}", e.getMessage(), e);
			throw e;
		}
	}

	public Optional<AuthUser> getOwnerByRestaurantId(String restaurantId) {
		logger.info("Entering getOwnerByRestaurantId() with restaurantId={}", restaurantId);
		try {
			Optional<AuthUser> result = userRepository.findByRestaurantIdAndRole(restaurantId, UserRole.OWNER);
			logger.info("Exiting getOwnerByRestaurantId()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in getOwnerByRestaurantId(): {}", e.getMessage(), e);
			throw e;
		}
	}

	public List<AuthUser> getStaffByRestaurant(String restaurantId) {
		logger.info("Entering getStaffByRestaurant() with restaurantId={}", restaurantId);
		try {
			List<AuthUser> result = userRepository.findByRestaurantId(restaurantId)
					.orElse(Collections.emptyList())
					.stream()
					.filter(u -> u.getRole() != UserRole.OWNER)
					.collect(Collectors.toList());
			logger.info("Exiting getStaffByRestaurant()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in getStaffByRestaurant(): {}", e.getMessage(), e);
			throw e;
		}
	}

	public AuthUser createStaff(AuthUser request) {
		logger.info("Entering createStaff()");
		try {
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
			logger.info("Exiting createStaff()");
			return saved;
		} catch (RuntimeException e) {
			logger.error("Error in createStaff(): {}", e.getMessage(), e);
			throw e;
		}
	}

	public boolean resendTempPassword(String username) {
		logger.info("Entering resendTempPassword()");
		try {
			Optional<AuthUser> optUser = userRepository.findByUsername(username);
			if (optUser.isEmpty()) {
				logger.info("Exiting resendTempPassword()");
				return false;
			}
			AuthUser user = optUser.get();
			String tempPassword = generateTempPassword();
			user.setPassword(tempPassword);
			user.setMustResetPassword(true);
			userRepository.save(user);
			boolean result = sendStaffCredentials(user.getEmail(), user.getName(), user.getUsername(), tempPassword);
			logger.info("Exiting resendTempPassword()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in resendTempPassword(): {}", e.getMessage(), e);
			throw e;
		}
	}

	public Optional<AuthUser> resetTempPassword(String username, String newPassword) {
		logger.info("Entering resetTempPassword()");
		try {
			Optional<AuthUser> optUser = userRepository.findByUsername(username);
			if (optUser.isEmpty()) {
				logger.info("Exiting resetTempPassword()");
				return Optional.empty();
			}
			AuthUser user = optUser.get();
			user.setPassword(newPassword);
			user.setMustResetPassword(false);
			Optional<AuthUser> result = Optional.of(userRepository.save(user));
			logger.info("Exiting resetTempPassword()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in resetTempPassword(): {}", e.getMessage(), e);
			throw e;
		}
	}

	public String generateTempPassword() {
		logger.info("Entering generateTempPassword()");
		try {
			String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
			Random random = new Random();
			StringBuilder sb = new StringBuilder(8);
			for (int i = 0; i < 8; i++) {
				sb.append(chars.charAt(random.nextInt(chars.length())));
			}
			String result = sb.toString();
			logger.info("Exiting generateTempPassword()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in generateTempPassword(): {}", e.getMessage(), e);
			throw e;
		}
	}

	public boolean sendStaffCredentials(String toEmail, String name, String username, String tempPassword) {
		logger.info("Entering sendStaffCredentials()");
		try {
			if (toEmail == null || toEmail.isBlank()) {
				logger.info("Exiting sendStaffCredentials()");
				return false;
			}
			if (mailSender == null) {
				logger.warn("JavaMailSender not configured — skipping email");
				logger.info("Exiting sendStaffCredentials()");
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
				logger.info("Exiting sendStaffCredentials()");
				return true;
			} catch (Exception e) {
				logger.error("Failed to send staff credentials email: {}", e.getMessage(), e);
				logger.info("Exiting sendStaffCredentials()");
				return false;
			}
		} catch (RuntimeException e) {
			logger.error("Error in sendStaffCredentials(): {}", e.getMessage(), e);
			throw e;
		}
	}


	public SignUpResponse saveUser(SignupForm form) {
		logger.info("Entering saveUser()");
		try {
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

			logger.info("Exiting saveUser()");
			return response;
		} catch (RuntimeException e) {
			logger.error("Error in saveUser(): {}", e.getMessage(), e);
			throw e;
		}
	}
}
