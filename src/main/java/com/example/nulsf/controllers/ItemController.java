package com.example.nulsf.controllers;

import com.example.nulsf.model.Item;
import com.example.nulsf.model.User;
import com.example.nulsf.repository.ItemRepository;
import com.example.nulsf.repository.UserRepository;
import com.example.nulsf.services.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@Controller
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository; // Added to fetch real user IDs

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/api/items/report-form")
    public String submitReport(
            @RequestParam("itemName") String itemName,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("color") String color,
            @RequestParam("status") String status,
            @RequestParam("specificLocation") String location,
            @RequestParam("specificDate") String date,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        // 1. Get the current logged-in user details
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName(); 
        
        // Fetch the user from DB to get their actual ID and Full Name
        // We look by email or username depending on your security config
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseGet(() -> userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found in database")));

        // 2. Map form data to Item Entity
        Item item = new Item();
        item.setItemName(itemName);
        item.setCategory(category);
        item.setDescription(description);
        item.setColor(color);
        item.setLocation(location);
        item.setDateReported(LocalDate.parse(date));
        
        // SET REAL USER DATA (Fixes the "Always Admin" bug)
        item.setPostedBy(currentUser.getFullName()); 
        item.setReportedBy(currentUser.getUserId().intValue()); 

        // 3. Status Logic (Syncs with AdminDashboard intake tab)
        if ("LOST".equalsIgnoreCase(status)) {
            item.setStatus("PENDING_CLAIM");
        } else {
            item.setStatus("PENDING_SURRENDER");
        }

        // 4. Handle Image Storage
        if (image != null && !image.isEmpty()) {
            fileStorageService.storeImage(image).ifPresent(filename -> {
                item.setImagePath("/uploads/" + filename);
            });
        }

        // 5. Save to Database
        itemRepository.save(item);

        return "redirect:/catalog?success";
    }
}