package com.example.nulsf.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nulsf.model.User;
import com.example.nulsf.repository.ClaimRepository;
import com.example.nulsf.repository.ItemRepository;
import com.example.nulsf.repository.UserRepository;
import com.example.nulsf.services.CurrentUserHelper;
import com.example.nulsf.services.MessagingService;
import com.example.nulsf.services.NotificationService;

@Controller
@RequestMapping("/profile")
public class ProfileWebController {

    @Autowired
    private CurrentUserHelper currentUserHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String profilePage(Model model) {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null) return "redirect:/auth/login";

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("unreadMessages", messagingService.getUnreadCount(currentUser));
        model.addAttribute("unreadNotifications", notificationService.getUnreadCount(currentUser));

        int reportsCount = itemRepository.findByReportedByOrderByItemIdDesc(currentUser.getUserId().intValue()).size();
        int claimsCount = claimRepository.findByClaimerOrderByClaimDateDesc(currentUser).size();

        model.addAttribute("reportsCount", reportsCount);
        model.addAttribute("claimsCount", claimsCount);

        return "Profile";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam(value = "contactNumber", required = false) String contactNumber,
                                @RequestParam(value = "studentId", required = false) String studentId,
                                @RequestParam(value = "department", required = false) String department,
                                RedirectAttributes redirectAttributes) {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null) return "redirect:/auth/login";

        currentUser.setFullName(fullName != null ? fullName.trim() : currentUser.getFullName());
        currentUser.setContactNumber(contactNumber != null ? contactNumber.trim() : "");
        currentUser.setStudentId(studentId != null ? studentId.trim() : "");
        currentUser.setDepartment(department != null ? department.trim() : "");

        userRepository.save(currentUser);
        redirectAttributes.addFlashAttribute("alertSuccess", "Profile updated successfully.");

        return "redirect:/profile";
    }
}
