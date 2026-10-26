package com.example.nulsf.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.nulsf.model.Conversation;
import com.example.nulsf.model.Message;
import com.example.nulsf.model.User;
import com.example.nulsf.repository.ConversationRepository;
import com.example.nulsf.repository.UserRepository;
import com.example.nulsf.services.MessagingService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conversations")
@CrossOrigin(origins = "*")
public class ConversationRestController {

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listConversations(@RequestParam(value = "userId", required = false) Long userId) {
        List<Conversation> list;
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.isAdmin()) {
                list = messagingService.getAllConversationsForAdmin();
            } else if (user != null) {
                list = messagingService.getUserConversations(user);
            } else {
                list = conversationRepository.findAll();
            }
        } else {
            list = conversationRepository.findAll();
        }

        List<Map<String, Object>> result = list.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("conversationId", c.getConversationId());
            map.put("itemId", c.getItem() != null ? c.getItem().getItemId() : 0);
            map.put("otherParticipantName", c.getUser() != null ? c.getUser().getFullName() : "Anonymous");
            map.put("subject", c.getSubject());
            map.put("lastMessage", c.getLastMessage());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Map<String, Object>>> getMessages(@PathVariable("id") Long convId) {
        Conversation conv = messagingService.getConversation(convId).orElse(null);
        if (conv == null) {
            return ResponseEntity.notFound().build();
        }

        List<Message> messages = messagingService.getMessages(conv);
        List<Map<String, Object>> result = messages.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("messageId", m.getMessageId());
            map.put("senderId", m.getSender().getUserId());
            map.put("senderName", m.getSender().getFullName());
            map.put("senderRole", m.getSenderRole());
            map.put("messageText", m.getMessageText());
            map.put("sentAt", m.getSentAt() != null ? m.getSentAt().toString() : "");
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<?> postMessage(@PathVariable("id") Long convId,
                                         @RequestBody Map<String, Object> body) {
        Long senderId = ((Number) body.get("senderId")).longValue();
        String text = (String) body.get("messageText");
        User sender = userRepository.findById(senderId).orElse(null);
        if (sender == null) return ResponseEntity.badRequest().body("Sender not found");

        Message msg = messagingService.sendMessage(convId, sender, text);
        return ResponseEntity.ok(Map.of("success", true, "messageId", msg.getMessageId()));
    }
}
