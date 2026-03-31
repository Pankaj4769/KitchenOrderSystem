package com.kos.controller;

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
            System.err.println("Google login error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/forgotPassword")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody UpdatePasswordRequest request) {
        AuthUser user = userService.getUserRoles(request.getUsername());
        if (user == null || user.getStaffId() == null) {
            return ResponseEntity.ok(new MessageResponse("failure", false));
        }
        user.setPassword(request.getNewPassword());
        boolean updated = userService.updatePassword(user);
        return ResponseEntity.ok(new MessageResponse(updated ? "success" : "failure", updated));
    }
    
    
}
