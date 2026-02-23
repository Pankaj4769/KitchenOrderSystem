package com.kos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kos.authentication.JwtUtil;
import com.kos.dto.AuthResponse;
import com.kos.dto.AuthUser;
import com.kos.dto.LoginRequest;
import com.kos.dto.SignUpResponse;
import com.kos.dto.SignupForm;
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
        	
        	SignupForm user = userService.getUser(request.getUsername());
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
    
    
    
}
