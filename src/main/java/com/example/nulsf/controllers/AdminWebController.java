package com.example.nulsf.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nulsf.model.Claim;
import com.example.nulsf.model.Item;
import com.example.nulsf.model.User;
import com.example.nulsf.repository.ItemRepository;
import com.example.nulsf.services.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ClaimService claimService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ReportExportService reportExportService;

    @Autowired
    private CurrentUserHelper currentUserHelper;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ItemRepository itemRepository;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "tab", defaultValue = "claims") String activeTab, Model model) {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/auth/login";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("stats", itemService.getSystemStatistics());
        model.addAttribute("pendingClaimsCount", claimService.countPendingClaims());

        // Tab Data
        List<Claim> pendingClaims = claimService.getPendingClaims();
        List<Claim> allClaims = claimService.getAllClaims();
        List<Item> pendingIntake = itemService.getItemsAwaitingIntake();
        List<Item> allItems = itemRepository.findAll();

        model.addAttribute("pendingClaims", pendingClaims);
        model.addAttribute("allClaims", allClaims);
        model.addAttribute("pendingIntake", pendingIntake);
        model.addAttribute("allItems", allItems);
        model.addAttribute("auditLogs", auditService.getRecentLogs());

        model.addAttribute("unreadMessages", messagingService.getUnreadCount(currentUser));
        model.addAttribute("unreadNotifications", notificationService.getUnreadCount(currentUser));

        return "AdminDashboard";
    }

    @PostMapping("/claims/{id}/approve")
    public String approveClaim(@PathVariable("id") Long claimId,
                               @RequestParam(value = "remarks", required = false) String remarks,
                               RedirectAttributes redirectAttributes) {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) return "redirect:/auth/login";

        try {
            claimService.approveClaim(claimId, currentUser, remarks);
            redirectAttributes.addFlashAttribute("alertSuccess", "Claim #" + claimId + " approved! Verification pickup pass issued to student.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertError", "Error: " + e.getMessage());
        }

        return "redirect:/admin/dashboard?tab=claims";
    }

    @PostMapping("/claims/{id}/reject")
    public String rejectClaim(@PathVariable("id") Long claimId,
                              @RequestParam(value = "remarks", required = false) String remarks,
                              RedirectAttributes redirectAttributes) {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) return "redirect:/auth/login";

        try {
            claimService.rejectClaim(claimId, currentUser, remarks);
            redirectAttributes.addFlashAttribute("alertSuccess", "Claim #" + claimId + " has been rejected with feedback.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertError", "Error: " + e.getMessage());
        }

        return "redirect:/admin/dashboard?tab=claims";
    }

    @PostMapping("/intake/{id}/receive")
    public String receiveIntake(@PathVariable("id") Integer itemId,
                                @RequestParam("storageLocation") String storageLocation,
                                @RequestParam(value = "notes", required = false) String notes,
                                RedirectAttributes redirectAttributes) {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) return "redirect:/auth/login";

        try {
            itemService.receiveFoundItem(itemId, currentUser, storageLocation, notes);
            redirectAttributes.addFlashAttribute("alertSuccess", "Item #" + itemId + " marked as RECEIVED and is now AVAILABLE in the catalog!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertError", "Error: " + e.getMessage());
        }

        return "redirect:/admin/dashboard?tab=intake";
    }

    @PostMapping("/items/{id}/status")
    public String updateItemStatus(@PathVariable("id") Integer itemId,
                                   @RequestParam("status") String status,
                                   @RequestParam(value = "notes", required = false) String notes,
                                   RedirectAttributes redirectAttributes) {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) return "redirect:/auth/login";

        try {
            itemService.updateStatus(itemId, status, currentUser, notes);
            redirectAttributes.addFlashAttribute("alertSuccess", "Item #" + itemId + " status updated to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertError", "Error: " + e.getMessage());
        }

        return "redirect:/admin/dashboard?tab=inventory";
    }

    @PostMapping("/items/{id}/delete")
    public String deleteItem(@PathVariable("id") Integer itemId,
                             RedirectAttributes redirectAttributes) {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) return "redirect:/auth/login";

        try {
            itemRepository.deleteById(itemId);
            auditService.log("ITEM_DELETED", currentUser.getEmail(), "ITEM", String.valueOf(itemId), "Deleted item record");
            redirectAttributes.addFlashAttribute("alertSuccess", "Item #" + itemId + " deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertError", "Could not delete item: " + e.getMessage());
        }

        return "redirect:/admin/dashboard?tab=inventory";
    }

    @GetMapping("/export/items.csv")
    public ResponseEntity<byte[]> exportItemsCsv() {
        byte[] csvData = reportExportService.generateItemsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"nu_lsf_items.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    @GetMapping("/export/claims.csv")
    public ResponseEntity<byte[]> exportClaimsCsv() {
        byte[] csvData = reportExportService.generateClaimsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"nu_lsf_claims.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}
