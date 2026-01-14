package com.kos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.kos.dto.User;


@RestController
public class LoginController {

//    @Autowired
//    UserRepository userRepository;
//
//    @PostMapping("/login")
//    public User login(@RequestBody User request) {
//
//        User user = userRepository.findById(request.getUsername())
//            .orElseThrow(() -> new RuntimeException("Invalid user"));
//
//        if (!user.getPassword().equals(request.getPassword())) {
//            throw new RuntimeException("Invalid password");
//        }
//
//        user.setPassword(null); // important: hide password
//        return user;
//    }
}
