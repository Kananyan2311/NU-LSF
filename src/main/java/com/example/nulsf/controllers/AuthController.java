package com.example.nulsf.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nulsf.services.AuthService;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String showAuthPage(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               @RequestParam(value = "registered", required = false) String registered,
                               Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("success", "Registration successful! You may now sign in with your credentials.");
        }
        return "login";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam(defaultValue = "USER") String role,
                           @RequestParam(required = false) String contactNumber,
                           @RequestParam(required = false) String studentId,
                           @RequestParam(required = false) String department,
                           @RequestParam(required = false) String adminKey,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        String result = authService.register(fullName, email, password, role, contactNumber, studentId, department, adminKey);

        if ("Registered Successfully!".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Registration successful as " + role.toUpperCase() + "! You can now log in.");
            return "redirect:/auth/login?registered=true";
        } else {
            model.addAttribute("registerError", result);
            model.addAttribute("tab", "register");
            model.addAttribute("prevRole", role);
            model.addAttribute("prevFullName", fullName);
            model.addAttribute("prevEmail", email);
            model.addAttribute("prevContact", contactNumber);
            model.addAttribute("prevStudentId", studentId);
            model.addAttribute("prevDepartment", department);
            return "login";
        }
    }
}
