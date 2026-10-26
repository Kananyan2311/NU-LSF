package com.example.nulsf.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nulsf.model.User;
import com.example.nulsf.services.CurrentUserHelper;
import com.example.nulsf.services.ItemService;
import com.example.nulsf.services.MessagingService;
import com.example.nulsf.services.NotificationService;

@Controller
public class HomeController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private CurrentUserHelper currentUserHelper;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/")
    public String home(Model model) {
        User currentUser = currentUserHelper.getCurrentUser();
        model.addAttribute("currentUser", currentUser);

        if (currentUser != null) {
            model.addAttribute("unreadMessages", messagingService.getUnreadCount(currentUser));
            model.addAttribute("unreadNotifications", notificationService.getUnreadCount(currentUser));
        } else {
            model.addAttribute("unreadMessages", 0);
            model.addAttribute("unreadNotifications", 0);
        }

        model.addAttribute("lostItems", itemService.getRecentLostItems());
        model.addAttribute("foundItems", itemService.getRecentFoundItems());
        model.addAttribute("stats", itemService.getSystemStatistics());

        return "Home";
    }
}
