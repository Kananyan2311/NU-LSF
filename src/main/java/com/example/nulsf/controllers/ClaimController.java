package com.example.nulsf.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nulsf.model.Claim;
import com.example.nulsf.model.User;
import com.example.nulsf.services.ClaimService;
import com.example.nulsf.services.CurrentUserHelper;
import com.example.nulsf.services.MessagingService;
import com.example.nulsf.services.NotificationService;

import java.util.List;

@Controller
@RequestMapping("/claims")
public class ClaimController {

    @Autowired
    private ClaimService claimService;

    @Autowired
    private CurrentUserHelper currentUserHelper;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/submit")
    public String submitClaim(@RequestParam("itemId") Integer itemId,
                              @RequestParam("proofDescription") String proofDescription,
                              RedirectAttributes redirectAttributes) {

        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        try {
            claimService.fileClaim(itemId, currentUser, proofDescription);
            redirectAttributes.addFlashAttribute("alertSuccess", "Claim filed successfully! The NU LSF Admin team will review your proof.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertError", e.getMessage());
        }

        return "redirect:/claims/my-claims";
    }

    @GetMapping("/my-claims")
    public String myClaims(Model model) {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        List<Claim> claims = claimService.getUserClaims(currentUser);
        model.addAttribute("claims", claims);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("unreadMessages", messagingService.getUnreadCount(currentUser));
        model.addAttribute("unreadNotifications", notificationService.getUnreadCount(currentUser));

        return "MyClaims";
    }

    @GetMapping("/{id}/pass")
    public String claimPass(@PathVariable("id") Long claimId, Model model) {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        Claim claim = claimService.getClaim(claimId).orElse(null);
        if (claim == null) {
            return "redirect:/claims/my-claims";
        }

        // Only claimant or admin can view pass
        if (!claim.getClaimer().getUserId().equals(currentUser.getUserId()) && !currentUser.isAdmin()) {
            return "redirect:/claims/my-claims";
        }

        model.addAttribute("claim", claim);
        model.addAttribute("currentUser", currentUser);

        return "ClaimPass";
    }
}
