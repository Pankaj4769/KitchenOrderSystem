package com.kos.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    UserService userService;

    @Autowired
    OtpService otpService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
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
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new MessageResponse("Your account is not linked to a restaurant. Contact your owner.", false));
                    }
                    java.util.Optional<AuthUser> ownerOpt = userService.getOwnerByRestaurantId(restaurantId);
                    boolean ownerReady = ownerOpt
                            .map(o -> o.getOnboardingStatus() == com.kos.dto.OnboardingStatus.SETUP_COMPLETE)
                            .orElse(false);
                    if (!ownerReady) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new MessageResponse("Restaurant setup is not complete. Please contact your owner.", false));
                    }
                }

                String token = jwtUtil.generateToken(request.getUsername());
                return ResponseEntity.ok(new AuthResponse(token));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PutMapping("/resetTempPassword")
    public ResponseEntity<?> resetTempPassword(@RequestBody ResetTempPasswordRequest request) {
        return userService.resetTempPassword(request.getUsername(), request.getNewPassword())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("User not found", false)));
    }

    @GetMapping("/getUser/{username}")
    public ResponseEntity<AuthUser> getUser(@PathVariable String username) {
        if (username != null) {
        	AuthUser user = userService.getUserRoles(username);
        	if(user != null && user.getStaffId() != null) {
	            return ResponseEntity.ok(user);
        	}
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/getUserByEmail/{email}")
    public ResponseEntity<AuthUser> getUserByEmail(@PathVariable String email) {
        Optional<AuthUser> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/signUp")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignupForm form) {
    	return ResponseEntity.ok(userService.saveUser(form));
    }

    @PostMapping("/addStaff")
    public ResponseEntity<AuthUser> addStaff(@RequestBody AuthUser request) {
        return ResponseEntity.ok(userService.createStaff(request));
    }

    @GetMapping("/staff/{restaurantId}")
    public ResponseEntity<List<AuthUser>> getStaffByRestaurant(@PathVariable String restaurantId) {
        List<AuthUser> staff = userService.getStaffByRestaurant(restaurantId);
        return ResponseEntity.ok(staff);
    }

    @PostMapping("/resendTempPassword")
    public ResponseEntity<MessageResponse> resendTempPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(new MessageResponse("username is required", false));
        }
        boolean sent = userService.resendTempPassword(username);
        return ResponseEntity.ok(new MessageResponse(sent ? "Temporary password sent" : "Failed to send", sent));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthUser> googleLogin(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String name  = body.get("name");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            AuthUser user  = userService.findOrCreateByGoogle(email, name);
            String   token = jwtUtil.generateToken(user.getUsername());
            user.setToken(token);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── OTP endpoints ──────────────────────────────────────────

    @PostMapping("/sendOtp")
    public ResponseEntity<MessageResponse> sendOtp(@RequestBody OtpRequest request) {
        boolean sent = otpService.sendOtp(request.getIdentifier(), request.getIdentifierType());
        return ResponseEntity.ok(new MessageResponse(sent ? "OTP sent" : "failure", sent));
    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<MessageResponse> verifyOtp(@RequestBody OtpVerifyRequest request) {
        boolean valid = otpService.verifyOtp(request.getIdentifier(), request.getIdentifierType(), request.getOtp());
        return ResponseEntity.ok(new MessageResponse(valid ? "success" : "Invalid or expired OTP", valid));
    }

    // ── Forgot Password ────────────────────────────────────────

    @PutMapping("/forgotPassword")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody UpdatePasswordRequest request) {
        Optional<AuthUser> optUser = userService.getUserByIdentifier(request.getIdentifier(), request.getIdentifierType());
        if (optUser.isEmpty()) {
            return ResponseEntity.ok(new MessageResponse("failure", false));
        }
        AuthUser user = optUser.get();
        user.setPassword(request.getNewPassword());
        boolean updated = userService.updatePassword(user);
        return ResponseEntity.ok(new MessageResponse(updated ? "success" : "failure", updated));
    }
}
