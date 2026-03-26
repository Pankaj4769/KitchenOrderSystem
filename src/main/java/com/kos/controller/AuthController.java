package com.kos.controller;

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
import com.kos.dto.SignUpResponse;
import com.kos.dto.SignupForm;
import com.kos.dto.UpdatePasswordRequest;
import com.kos.service.UserService;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        // Validate user manually or via AuthenticationManager
    	    	
        if (request != null) {
        	
        	AuthUser user = userService.getUser(request.getUsername());
        	AuthUser authUser = userService.getUserRoles(request.getUsername());
        	if(user.getUsername() != null && request.getPassword().equals(user.getPassword()) && request.getRole().equalsIgnoreCase(authUser.getRole().toString())) {
	            String token = jwtUtil.generateToken(request.getUsername());	
	            return ResponseEntity.ok(new AuthResponse(token));
        	}
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    
    @GetMapping("/getUser/{username}")
    public ResponseEntity<AuthUser> getUser(@PathVariable  String username) {
    	    	
        if (username != null) {
        	
        	AuthUser user = userService.getUserRoles(username);
        	if(user != null) {	
	            return ResponseEntity.ok(user);
        	}
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    
    @PostMapping("/signUp")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignupForm form) {	
    	return ResponseEntity.ok(userService.saveUser(form));	
    }
    
    @GetMapping("/checkUsername/{username}")
    public ResponseEntity<MessageResponse> checkUsername(@PathVariable String username) {
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Username is required", false));
        }

        AuthUser user = userService.getUser(username);
        if (user != null && user.getUsername() != null) {
            return ResponseEntity.ok(new MessageResponse("User exists", true));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse("User not found", false));
    }
    
    @PutMapping("/forgotPassword")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody UpdatePasswordRequest request) {
        if (request == null || request.getUsername() == null || request.getNewPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Username and new password are required", false));
        }

        AuthUser user = userService.getUser(request.getUsername());
        if (user == null || user.getUsername() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("User not found", false));
        }

        boolean updated = userService.updatePassword(request.getUsername(), request.getNewPassword());
        if (updated) {
            return ResponseEntity.ok(new MessageResponse("Password updated successfully", true));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Failed to update password", false));
    }
    
    
}
