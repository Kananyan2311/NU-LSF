package com.example.nulsf.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.nulsf.model.Notification;
import com.example.nulsf.model.User;
import com.example.nulsf.services.CurrentUserHelper;
import com.example.nulsf.services.MessagingService;
import com.example.nulsf.services.NotificationService;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationWebController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CurrentUserHelper currentUserHelper;

    @Autowired
    private MessagingService messagingService;

    @GetMapping
    public String notificationsPage(Model model) {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null) return "redirect:/auth/login";

        List<Notification> notifications = notificationService.getUserNotifications(currentUser);
        model.addAttribute("notifications", notifications);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("unreadMessages", messagingService.getUnreadCount(currentUser));
        model.addAttribute("unreadNotifications", notificationService.getUnreadCount(currentUser));

        return "Notifications";
    }

    @PostMapping("/{id}/read")
    public String markRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String markAllRead() {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser != null) {
            notificationService.markAllAsRead(currentUser);
        }
        return "redirect:/notifications";
    }
}
