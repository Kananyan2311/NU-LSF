package com.example.nulsf.controllers;

import com.example.nulsf.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * List all users (id, fullName, email, role) for admin messaging - no password.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listUsers() {
        List<Map<String, Object>> list = userRepository.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "userId", u.getUserId(),
                        "fullName", u.getFullName() != null ? u.getFullName() : "",
                        "email", u.getEmail() != null ? u.getEmail() : "",
                        "role", u.getRole() != null ? u.getRole() : ""
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
