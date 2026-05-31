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

import com.kos.admin.AdminTenantStatusReadOnly;
import com.kos.admin.AdminTenantStatusReadOnlyRepository;
import com.kos.authentication.JwtUtil;
import com.kos.authentication.PasswordUtil.PasswordHelper;
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

    @Autowired
    private com.kos.service.ProfileService profileService;
    
    @Autowired
    AdminTenantStatusReadOnlyRepository adminTenantStatusRepo;

    @Autowired
    PasswordHelper passwordHelper;

    /**
     * Returns a 403 ResponseEntity if the user's tenant is suspended in the
     * admin panel; null otherwise. Owners and staff alike are blocked when
     * the tenant they belong to is suspended.
     *
     * Null/blank/non-numeric restaurantId is treated as "not suspended"
     * (e.g. platform users without a tenant yet).
     */
    private ResponseEntity<?> tenantSuspensionResponseOrNull(String restaurantId) {
        if (restaurantId == null || restaurantId.isBlank()) return null;
        Integer tenantId;
        try {
            tenantId = Integer.valueOf(restaurantId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
        Optional<AdminTenantStatusReadOnly> row = adminTenantStatusRepo.findByTenantId(tenantId);
        if (row.isPresent() && "SUSPENDED".equalsIgnoreCase(row.get().getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse(
                            "Your restaurant has been suspended. Please contact support.",
                            false));
        }
        return null;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        logger.info("Entering login()");
        try {
            if (request != null) {
                AuthUser user = userService.getUser(request.getUsername());
                AuthUser authUser = userService.getUserRoles(request.getUsername());
                if (user.getUsername() != null
                        && passwordHelper.matches(request.getPassword(), user.getPassword())
                        && request.getRole().equalsIgnoreCase(authUser.getRole().toString())) {

                    if (!passwordHelper.isBcrypt(user.getPassword())) {
                        userService.rehashPassword(user, request.getPassword());
                    }

                    // Admin-panel suspension check (applies to owner + staff).
                    ResponseEntity<?> suspended = tenantSuspensionResponseOrNull(user.getRestaurantId());
                    if (suspended != null) {
                        logger.info("Exiting login() — tenant suspended");
                        return suspended;
                    }

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
                    response.put("language", user.getLanguage());
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
     * GET /auth/getUserByMobile/{mobile}
     * Returns 200 with user if a user with the given mobile exists, 404 otherwise.
     * Used by the signup form to warn the user that the mobile is already taken
     * before they waste an OTP on a duplicate signup.
     */
    @GetMapping("/getUserByMobile/{mobile}")
    public ResponseEntity<AuthUser> getUserByMobile(@PathVariable String mobile) {
        logger.info("Entering getUserByMobile()");
        try {
            Optional<AuthUser> user = userService.getUserByIdentifier(mobile, "mobile");
            ResponseEntity<AuthUser> result = user.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
            logger.info("Exiting getUserByMobile()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getUserByMobile(): {}", e.getMessage(), e);
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

    @PostMapping("/loginWithOtp")
    public ResponseEntity<?> loginWithOtp(@RequestBody Map<String, String> body) {
        logger.info("Entering loginWithOtp()");
        try {
            String mobile = body.get("mobile");
            String otp    = body.get("otp");
            if (mobile == null || mobile.isBlank() || otp == null || otp.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("mobile and otp are required", false));
            }
            boolean valid = otpService.verifyOtp(mobile, "mobile", otp);
            if (!valid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Invalid or expired OTP", false));
            }
            Optional<AuthUser> userOpt = userService.getUserByIdentifier(mobile, "mobile");
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("No account found for this mobile number", false));
            }
            AuthUser user = userOpt.get();

            // Mirror /auth/login's non-owner gate
            if (user.getRole() != com.kos.dto.UserRole.OWNER) {
                String restaurantId = user.getRestaurantId();
                if (restaurantId == null || restaurantId.isBlank()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Your account is not linked to a restaurant. Contact your owner.", false));
                }
                Optional<AuthUser> ownerOpt = userService.getOwnerByRestaurantId(restaurantId);
                boolean ownerReady = ownerOpt
                        .map(o -> o.getOnboardingStatus() == com.kos.dto.OnboardingStatus.SETUP_COMPLETE)
                        .orElse(false);
                if (!ownerReady) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Restaurant setup is not complete. Please contact your owner.", false));
                }
            }

            String token = jwtUtil.generateToken(user.getUsername());
            user.setToken(token);
            logger.info("Exiting loginWithOtp()");
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            logger.error("Error in loginWithOtp(): {}", e.getMessage(), e);
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

            // Admin-panel suspension check (Google path).
            ResponseEntity<?> suspended = tenantSuspensionResponseOrNull(user.getRestaurantId());
            if (suspended != null) {
                logger.info("Exiting googleLogin() — tenant suspended");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

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
            user.setPassword(passwordHelper.encode(request.getNewPassword()));
            boolean updated = userService.updatePassword(user);
            logger.info("Exiting forgotPassword()");
            return ResponseEntity.ok(new MessageResponse(updated ? "success" : "failure", updated));
        } catch (RuntimeException e) {
            logger.error("Error in forgotPassword(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // ── Profile settings (authenticated owner) ─────────────────

    @GetMapping("/profile/settings")
    public ResponseEntity<?> getProfileSettings(java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            return ResponseEntity.ok(profileService.getSettings(principal.getName()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage(), false));
        }
    }

    @PutMapping("/profile/contact")
    public ResponseEntity<?> updateContact(
            @RequestBody com.kos.dto.UpdateContactRequest req,
            java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            profileService.updateContact(principal.getName(), req);
            return ResponseEntity.ok(new MessageResponse("Contact updated", true));
        } catch (com.kos.service.ProfileService.VerificationRequiredException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse(e.getMessage(), false));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage(), false));
        }
    }

    @PutMapping("/profile/restaurant")
    public ResponseEntity<?> updateRestaurant(
            @RequestBody com.kos.dto.UpdateRestaurantRequest req,
            java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            return ResponseEntity.ok(profileService.updateRestaurant(principal.getName(), req));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage(), false));
        }
    }

    @GetMapping("/profile/language")
    public ResponseEntity<?> getLanguage(java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            String lang = profileService.getLanguage(principal.getName());
            return ResponseEntity.ok(java.util.Map.of("language", lang != null ? lang : "en"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage(), false));
        }
    }

    @PutMapping("/profile/language")
    public ResponseEntity<?> updateLanguage(
            @RequestBody com.kos.dto.LanguageUpdateRequest req,
            java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            profileService.updateLanguage(principal.getName(), req.getLanguage());
            return ResponseEntity.ok(new MessageResponse("Language updated", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage(), false));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage(), false));
        }
    }
}
