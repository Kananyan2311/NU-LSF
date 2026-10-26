package com.example.nulsf.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nulsf.model.Item;
import com.example.nulsf.model.User;
import com.example.nulsf.repository.ItemRepository;
import com.example.nulsf.services.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
public class CatalogController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private CurrentUserHelper currentUserHelper;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditService auditService;

    @GetMapping("/catalog")
    public String catalog(@RequestParam(value = "keyword", required = false) String keyword,
                          @RequestParam(value = "category", required = false) String category,
                          @RequestParam(value = "status", required = false) String status,
                          @RequestParam(value = "reported", required = false) String reported,
                          @RequestParam(value = "claimed", required = false) String claimed,
                          Model model) {

        User currentUser = currentUserHelper.getCurrentUser();
        model.addAttribute("currentUser", currentUser);

        if (currentUser != null) {
            model.addAttribute("unreadMessages", messagingService.getUnreadCount(currentUser));
            model.addAttribute("unreadNotifications", notificationService.getUnreadCount(currentUser));
        }

        List<Item> items = itemService.searchItems(keyword, category, status);
        model.addAttribute("items", items);

        model.addAttribute("selectedKeyword", keyword != null ? keyword : "");
        model.addAttribute("selectedCategory", category != null ? category : "ALL");
        model.addAttribute("selectedStatus", status != null ? status : "ALL");

        model.addAttribute("categories", Arrays.asList(
                "Electronics", 
                "IDs & Cards", 
                "Wallets & Bags", 
                "Keys", 
                "Books & Stationery", 
                "Clothing & Accessories", 
                "Personal Belongings",
                "Other"
        ));

        if (reported != null) {
            model.addAttribute("alertSuccess", "Your item report has been submitted successfully to NU LSF!");
        }
        if (claimed != null) {
            model.addAttribute("alertSuccess", "Your claim request has been filed! Admin will verify your ownership proof.");
        }

        return "Catalog";
    }

    @GetMapping("/items/{id}")
    public String itemDetail(@PathVariable("id") Integer id, Model model) {
        Item item = itemRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/catalog";
        }

        User currentUser = currentUserHelper.getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("item", item);

        // Smart Matches (Feature 7)
        List<ItemService.ItemMatchResult> smartMatches = itemService.findPotentialMatches(item);
        model.addAttribute("smartMatches", smartMatches);

        if (currentUser != null) {
            model.addAttribute("unreadMessages", messagingService.getUnreadCount(currentUser));
            model.addAttribute("unreadNotifications", notificationService.getUnreadCount(currentUser));
        }

        return "ItemDetail";
    }

    @GetMapping("/report")
    public String reportPage(@RequestParam(value = "type", required = false) String type, Model model) {
        User currentUser = currentUserHelper.getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("reportType", type != null ? type.toUpperCase() : null);

        model.addAttribute("categories", Arrays.asList(
                "Electronics", 
                "IDs & Cards", 
                "Wallets & Bags", 
                "Keys", 
                "Books & Stationery", 
                "Clothing & Accessories", 
                "Personal Belongings",
                "Other"
        ));

        return "Report";
    }

    @PostMapping("/report/submit")
    public String submitReport(@RequestParam("itemName") String itemName,
                               @RequestParam("category") String category,
                               @RequestParam("description") String description,
                               @RequestParam(value = "color", required = false) String color,
                               @RequestParam("status") String statusType, // "LOST" or "FOUND"
                               @RequestParam("specificLocation") String location,
                               @RequestParam("specificDate") String dateStr,
                               @RequestParam(value = "turnedInTo", required = false) String turnedInTo,
                               @RequestParam(value = "image", required = false) MultipartFile image,
                               RedirectAttributes redirectAttributes) {

        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        Item item = new Item();
        item.setItemName(itemName.trim());
        item.setCategory(category.trim());
        item.setDescription(description != null ? description.trim() : "");
        item.setColor(color != null ? color.trim() : "");
        item.setLocation(location.trim());
        item.setTurnedInTo(turnedInTo);

        try {
            item.setDateReported(LocalDate.parse(dateStr));
        } catch (Exception e) {
            item.setDateReported(LocalDate.now());
        }

        item.setPostedBy(currentUser.getFullName());
        item.setReportedBy(currentUser.getUserId().intValue());

        if ("LOST".equalsIgnoreCase(statusType)) {
            item.setStatus("PENDING_CLAIM"); // Report lost item
        } else {
            item.setStatus("PENDING_SURRENDER"); // Report found item awaiting office intake
        }

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            fileStorageService.storeImage(image).ifPresent(filename -> {
                item.setImagePath("/uploads/" + filename);
            });
        }

        Item savedItem = itemRepository.save(item);

        auditService.log("ITEM_REPORTED", currentUser.getEmail(), "ITEM", String.valueOf(savedItem.getItemId()),
                "Reported as " + statusType + ": " + savedItem.getItemName());

        // Check for Smart Match and notify user
        List<ItemService.ItemMatchResult> matches = itemService.findPotentialMatches(savedItem);
        if (!matches.isEmpty()) {
            Item topMatch = matches.get(0).getItem();
            notificationService.notifyUser(
                    currentUser,
                    "Possible Match Found For Your Item!",
                    "We found a potential match: '" + topMatch.getItemName() + "' (" + matches.get(0).score + "% similarity). Check it in the catalog!",
                    "/items/" + topMatch.getItemId(),
                    "MATCH_FOUND"
            );
        }

        redirectAttributes.addFlashAttribute("alertSuccess", "Report registered successfully for Item #" + savedItem.getItemId());
        return "redirect:/items/" + savedItem.getItemId();
    }
}
