package com.kos.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kos.authentication.JwtUtil;
import com.kos.dto.AuthResponse;
import com.kos.dto.AuthUser;
import com.kos.dto.LoginRequest;
import com.kos.dto.MessageResponse;
import com.kos.dto.OtpRequest;
import com.kos.dto.OtpVerifyRequest;
import com.kos.dto.ResetTempPasswordRequest;
import com.kos.dto.SignUpResponse;
import com.kos.dto.SignupForm;
import com.kos.dto.UpdatePasswordRequest;
import com.kos.service.OtpService;
import com.kos.service.UserService;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LogManager.getLogger(AuthController.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    UserService userService;

    @Autowired
    OtpService otpService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        logger.info("Entering login()");
        try {
            if (request != null) {
                AuthUser user = userService.getUser(request.getUsername());
                AuthUser authUser = userService.getUserRoles(request.getUsername());
                if (user.getUsername() != null && request.getPassword().equals(user.getPassword())
                        && request.getRole().equalsIgnoreCase(authUser.getRole().toString())) {

                    // Staff-only restriction: owner must have completed setup
                    com.kos.dto.UserRole role = authUser.getRole();
                    if (role != com.kos.dto.UserRole.OWNER) {
                        String restaurantId = user.getRestaurantId();
                        if (restaurantId == null || restaurantId.isBlank()) {
                            logger.info("Exiting login()");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                    .body(new MessageResponse("Your account is not linked to a restaurant. Contact your owner.", false));
                        }
                        java.util.Optional<AuthUser> ownerOpt = userService.getOwnerByRestaurantId(restaurantId);
                        boolean ownerReady = ownerOpt
                                .map(o -> o.getOnboardingStatus() == com.kos.dto.OnboardingStatus.SETUP_COMPLETE)
                                .orElse(false);
                        if (!ownerReady) {
                            logger.info("Exiting login()");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                    .body(new MessageResponse("Restaurant setup is not complete. Please contact your owner.", false));
                        }
                    }

                    String token = jwtUtil.generateToken(request.getUsername());
                    logger.info("Exiting login()");
                    return ResponseEntity.ok(new AuthResponse(token));
                }
            }
            logger.info("Exiting login()");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (RuntimeException e) {
            logger.error("Error in login(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/resetTempPassword")
    public ResponseEntity<?> resetTempPassword(@RequestBody ResetTempPasswordRequest request) {
        logger.info("Entering resetTempPassword()");
        try {
            ResponseEntity<?> result = userService.resetTempPassword(request.getUsername(), request.getNewPassword())
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new MessageResponse("User not found", false)));
            logger.info("Exiting resetTempPassword()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in resetTempPassword(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/getUser/{username}")
    public ResponseEntity<?> getUser(@PathVariable String username) {
        logger.info("Entering getUser()");
        try {
            if (username != null) {
                AuthUser user = userService.getUserRoles(username);
                if (user != null && user.getStaffId() != null) {
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("staffId", user.getStaffId());
                    response.put("name", user.getName());
                    response.put("username", user.getUsername());
                    response.put("email", user.getEmail());
                    response.put("mobile", user.getMobile());
                    response.put("role", user.getRole());
                    response.put("token", user.getToken());
                    response.put("isFirstTime", user.isFirstTime());
                    response.put("firstTime", user.isFirstTime());
                    response.put("onboardingStatus", user.getOnboardingStatus());
                    response.put("mustResetPassword", user.isMustResetPassword());
                    response.put("subscriptionPlan", user.getSubscriptionPlan());
                    response.put("restaurantId", user.getRestaurantId());
                    response.put("restaurantName", user.getRestaurantName());
                    logger.info("Exiting getUser()");
                    return ResponseEntity.ok(response);
                }
            }
            logger.info("Exiting getUser()");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            logger.error("Error in getUser(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/getUserByEmail/{email}")
    public ResponseEntity<AuthUser> getUserByEmail(@PathVariable String email) {
        logger.info("Entering getUserByEmail()");
        try {
            Optional<AuthUser> user = userService.getUserByEmail(email);
            ResponseEntity<AuthUser> result = user.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
            logger.info("Exiting getUserByEmail()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getUserByEmail(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * GET /auth/staff/{restaurantId}
     * Returns all staff members registered under the given restaurant.
     * Used by the POS waiter selection popup to list available waiters.
     */
    @GetMapping("/staff/{restaurantId}")
    public ResponseEntity<List<AuthUser>> getStaffByRestaurant(@PathVariable String restaurantId) {
        logger.info("Entering getStaffByRestaurant() with restaurantId={}", restaurantId);
        try {
            List<AuthUser> staff = userService.getStaffByRestaurant(restaurantId);
            logger.info("Exiting getStaffByRestaurant()");
            return ResponseEntity.ok(staff);
        } catch (RuntimeException e) {
            logger.error("Error in getStaffByRestaurant(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/signUp")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignupForm form) {
        logger.info("Entering signUp()");
        try {
            ResponseEntity<SignUpResponse> result = ResponseEntity.ok(userService.saveUser(form));
            logger.info("Exiting signUp()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in signUp(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/addStaff")
    public ResponseEntity<AuthUser> addStaff(@RequestBody AuthUser request) {
        logger.info("Entering addStaff()");
        try {
            ResponseEntity<AuthUser> result = ResponseEntity.ok(userService.createStaff(request));
            logger.info("Exiting addStaff()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in addStaff(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/resendTempPassword")
    public ResponseEntity<MessageResponse> resendTempPassword(@RequestBody Map<String, String> body) {
        logger.info("Entering resendTempPassword()");
        try {
            String username = body.get("username");
            if (username == null || username.isBlank()) {
                logger.info("Exiting resendTempPassword()");
                return ResponseEntity.badRequest().body(new MessageResponse("username is required", false));
            }
            boolean sent = userService.resendTempPassword(username);
            logger.info("Exiting resendTempPassword()");
            return ResponseEntity.ok(new MessageResponse(sent ? "Temporary password sent" : "Failed to send", sent));
        } catch (RuntimeException e) {
            logger.error("Error in resendTempPassword(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/google")
    public ResponseEntity<AuthUser> googleLogin(@RequestBody Map<String, String> body) {
        logger.info("Entering googleLogin()");
        try {
            String email = body.get("email");
            String name  = body.get("name");
            if (email == null || email.isEmpty()) {
                logger.info("Exiting googleLogin()");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            AuthUser user  = userService.findOrCreateByGoogle(email, name);
            String   token = jwtUtil.generateToken(user.getUsername());
            user.setToken(token);
            logger.info("Exiting googleLogin()");
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error in googleLogin(): {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── OTP endpoints ──────────────────────────────────────────

    @PostMapping("/sendOtp")
    public ResponseEntity<MessageResponse> sendOtp(@RequestBody OtpRequest request) {
        logger.info("Entering sendOtp()");
        try {
            boolean sent = otpService.sendOtp(request.getIdentifier(), request.getIdentifierType());
            logger.info("Exiting sendOtp()");
            return ResponseEntity.ok(new MessageResponse(sent ? "OTP sent" : "failure", sent));
        } catch (RuntimeException e) {
            logger.error("Error in sendOtp(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<MessageResponse> verifyOtp(
            @RequestBody OtpVerifyRequest request,
            java.security.Principal principal) {
        logger.info("Entering verifyOtp()");
        try {
            boolean valid = otpService.verifyOtp(
                request.getIdentifier(),
                request.getIdentifierType(),
                request.getOtp()
            );
            if (valid && principal != null && principal.getName() != null) {
                otpService.markVerified(principal.getName(), request.getIdentifier());
            }
            return ResponseEntity.ok(new MessageResponse(valid ? "success" : "Invalid or expired OTP", valid));
        } catch (RuntimeException e) {
            logger.error("Error in verifyOtp(): {}", e.getMessage(), e);
            throw e;
        } finally {
            logger.info("Exiting verifyOtp()");
        }
    }

    // ── Forgot Password ────────────────────────────────────────

    @PutMapping("/forgotPassword")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody UpdatePasswordRequest request) {
        logger.info("Entering forgotPassword()");
        try {
            Optional<AuthUser> optUser = userService.getUserByIdentifier(request.getIdentifier(), request.getIdentifierType());
            if (optUser.isEmpty()) {
                logger.info("Exiting forgotPassword()");
                return ResponseEntity.ok(new MessageResponse("failure", false));
            }
            AuthUser user = optUser.get();
            user.setPassword(request.getNewPassword());
            boolean updated = userService.updatePassword(user);
            logger.info("Exiting forgotPassword()");
            return ResponseEntity.ok(new MessageResponse(updated ? "success" : "failure", updated));
        } catch (RuntimeException e) {
            logger.error("Error in forgotPassword(): {}", e.getMessage(), e);
            throw e;
        }
    }
}
