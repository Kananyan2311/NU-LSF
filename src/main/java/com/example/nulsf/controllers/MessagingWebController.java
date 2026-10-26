package com.example.nulsf.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nulsf.model.Conversation;
import com.example.nulsf.model.Item;
import com.example.nulsf.model.Message;
import com.example.nulsf.model.User;
import com.example.nulsf.repository.ItemRepository;
import com.example.nulsf.services.CurrentUserHelper;
import com.example.nulsf.services.MessagingService;
import com.example.nulsf.services.NotificationService;

import java.util.List;

@Controller
public class MessagingWebController {

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private CurrentUserHelper currentUserHelper;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private NotificationService notificationService;

    // --- STUDENT / USER MESSAGES ---

    @GetMapping("/messages")
    public String userMessages(@RequestParam(value = "id", required = false) Long conversationId, Model model) {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null) return "redirect:/auth/login";

        List<Conversation> conversations = messagingService.getUserConversations(currentUser);
        model.addAttribute("conversations", conversations);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAdminView", false);
        model.addAttribute("unreadMessages", messagingService.getUnreadCount(currentUser));
        model.addAttribute("unreadNotifications", notificationService.getUnreadCount(currentUser));

        Conversation activeConversation = null;
        if (conversationId != null) {
            activeConversation = messagingService.getConversation(conversationId)
                    .filter(c -> c.getUser().getUserId().equals(currentUser.getUserId()))
                    .orElse(null);
        } else if (!conversations.isEmpty()) {
            activeConversation = conversations.get(0);
        }

        if (activeConversation != null) {
            messagingService.markMessagesAsRead(activeConversation, currentUser);
            List<Message> messages = messagingService.getMessages(activeConversation);
            model.addAttribute("activeConversation", activeConversation);
            model.addAttribute("messages", messages);
        }

        return "Messages";
    }

    @GetMapping("/messages/{id}")
    public String userConversationDetail(@PathVariable("id") Long id) {
        return "redirect:/messages?id=" + id;
    }

    @PostMapping("/messages/send")
    public String sendUserMessage(@RequestParam("conversationId") Long conversationId,
                                  @RequestParam("messageText") String messageText,
                                  RedirectAttributes redirectAttributes) {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null) return "redirect:/auth/login";

        if (messageText != null && !messageText.trim().isEmpty()) {
            try {
                messagingService.sendMessage(conversationId, currentUser, messageText.trim());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("alertError", "Failed to send message: " + e.getMessage());
            }
        }
        return "redirect:/messages?id=" + conversationId;
    }

    @PostMapping("/messages/start")
    public String startConversation(@RequestParam(value = "itemId", required = false) Integer itemId,
                                    @RequestParam(value = "subject", required = false) String subject,
                                    @RequestParam("messageText") String messageText) {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null) return "redirect:/auth/login";

        Item item = (itemId != null) ? itemRepository.findById(itemId).orElse(null) : null;
        String topic = (subject != null && !subject.trim().isEmpty()) ? subject.trim() : 
                       (item != null ? "Inquiry regarding " + item.getItemName() : "General Question to Admin");

        Conversation conv = messagingService.startOrGetConversation(currentUser, item, topic, messageText);
        return "redirect:/messages?id=" + conv.getConversationId();
    }

    // --- ADMIN MESSAGES PORTAL ---

    @GetMapping("/admin/messages")
    public String adminMessages(@RequestParam(value = "id", required = false) Long conversationId, Model model) {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) return "redirect:/auth/login";

        List<Conversation> conversations = messagingService.getAllConversationsForAdmin();
        model.addAttribute("conversations", conversations);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAdminView", true);
        model.addAttribute("unreadMessages", messagingService.getUnreadCount(currentUser));
        model.addAttribute("unreadNotifications", notificationService.getUnreadCount(currentUser));

        Conversation activeConversation = null;
        if (conversationId != null) {
            activeConversation = messagingService.getConversation(conversationId).orElse(null);
        } else if (!conversations.isEmpty()) {
            activeConversation = conversations.get(0);
        }

        if (activeConversation != null) {
            messagingService.markMessagesAsRead(activeConversation, currentUser);
            List<Message> messages = messagingService.getMessages(activeConversation);
            model.addAttribute("activeConversation", activeConversation);
            model.addAttribute("messages", messages);
        }

        return "Messages";
    }

    @GetMapping("/admin/messages/{id}")
    public String adminConversationDetail(@PathVariable("id") Long id) {
        return "redirect:/admin/messages?id=" + id;
    }

    @PostMapping("/admin/messages/send")
    public String sendAdminMessage(@RequestParam("conversationId") Long conversationId,
                                   @RequestParam("messageText") String messageText,
                                   RedirectAttributes redirectAttributes) {
        User currentUser = currentUserHelper.getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) return "redirect:/auth/login";

        if (messageText != null && !messageText.trim().isEmpty()) {
            try {
                messagingService.sendMessage(conversationId, currentUser, messageText.trim());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("alertError", "Failed to send message: " + e.getMessage());
            }
        }
        return "redirect:/admin/messages?id=" + conversationId;
    }
}
