package com.example.nulsf.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.nulsf.model.User;
import com.example.nulsf.repository.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.passcode:NU-ADMIN-2026}")
    private String adminPasscode;

    /**
     * Registers a new User or Admin.
     * If role is ADMIN, checks adminPasscode.
     */
    public String register(String fullName,
                           String email,
                           String password,
                           String role,
                           String contactNumber,
                           String studentId,
                           String department,
                           String adminKey) {

        if (email == null || email.trim().isEmpty()) {
            return "Email is required!";
        }
        String cleanEmail = email.trim().toLowerCase();

        if (password == null || password.length() < 6) {
            return "Password must be at least 6 characters!";
        }

        if (userRepository.findByEmail(cleanEmail).isPresent() || userRepository.findByUsername(cleanEmail).isPresent()) {
            return "An account with email " + cleanEmail + " already exists!";
        }

        String assignedRole = "USER";
        if ("ADMIN".equalsIgnoreCase(role)) {
            if (adminKey == null || !adminPasscode.equalsIgnoreCase(adminKey.trim())) {
                return "Invalid Admin Clearance Passcode! Please contact NU LSF Security.";
            }
            assignedRole = "ADMIN";
        }

        User user = new User();
        user.setFullName(fullName != null ? fullName.trim() : "NU Bulldog");
        user.setEmail(cleanEmail);
        user.setUsername(cleanEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(assignedRole);
        user.setContactNumber(contactNumber != null ? contactNumber.trim() : "");
        user.setStudentId(studentId != null ? studentId.trim() : "");
        user.setDepartment(department != null ? department.trim() : "General");

        userRepository.save(user);

        return "Registered Successfully!";
    }

    public String getAdminPasscode() {
        return adminPasscode;
    }
}
