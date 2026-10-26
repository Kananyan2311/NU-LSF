package com.example.nulsf.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nulsf.model.Conversation;
import com.example.nulsf.model.Item;
import com.example.nulsf.model.Message;
import com.example.nulsf.model.User;
import com.example.nulsf.repository.ConversationRepository;
import com.example.nulsf.repository.MessageRepository;
import com.example.nulsf.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessagingService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditService auditService;

    @Transactional
    public Conversation startOrGetConversation(User user, Item item, String subject, String initialMessageText) {
        Conversation conversation = null;
        if (item != null) {
            Optional<Conversation> existing = conversationRepository.findByUserAndItem(user, item);
            if (existing.isPresent()) {
                conversation = existing.get();
            }
        }

        if (conversation == null) {
            conversation = new Conversation(user, item, subject != null ? subject : "Inquiry about Item #" + (item != null ? item.getItemId() : "General"));
            
            // Assign first admin if available
            userRepository.findFirstByRoleIgnoreCase("ADMIN").ifPresent(conversation::setAdmin);
            
            conversation = conversationRepository.save(conversation);
        }

        if (initialMessageText != null && !initialMessageText.trim().isEmpty()) {
            sendMessage(conversation.getConversationId(), user, initialMessageText.trim());
        }

        return conversation;
    }

    @Transactional
    public Message sendMessage(Long conversationId, User sender, String text) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        String senderRole = sender.isAdmin() ? "ADMIN" : "USER";
        Message msg = new Message(conv, sender, senderRole, text);
        messageRepository.save(msg);

        conv.setLastMessage(text.length() > 80 ? text.substring(0, 77) + "..." : text);
        conv.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conv);

        // Notify other party
        if (sender.isAdmin()) {
            // Notify student user
            notificationService.notifyUser(
                    conv.getUser(),
                    "New Message from NU LSF Admin",
                    "Admin replied to your inquiry: " + text,
                    "/messages/" + conv.getConversationId(),
                    "MESSAGE"
            );
        } else {
            // Notify admin
            if (conv.getAdmin() != null) {
                notificationService.notifyUser(
                        conv.getAdmin(),
                        "New Message from " + sender.getFullName(),
                        sender.getFullName() + ": " + text,
                        "/admin/messages/" + conv.getConversationId(),
                        "MESSAGE"
                );
            }
        }

        auditService.log("MESSAGE_SENT", sender.getEmail(), "CONVERSATION", conv.getConversationId().toString(), "Sent message in thread");

        return msg;
    }

    public List<Conversation> getUserConversations(User user) {
        return conversationRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    public List<Conversation> getAllConversationsForAdmin() {
        return conversationRepository.findAllByOrderByUpdatedAtDesc();
    }

    public Optional<Conversation> getConversation(Long id) {
        return conversationRepository.findById(id);
    }

    public List<Message> getMessages(Conversation conversation) {
        return messageRepository.findByConversationOrderBySentAtAsc(conversation);
    }

    @Transactional
    public void markMessagesAsRead(Conversation conversation, User reader) {
        List<Message> messages = messageRepository.findByConversationOrderBySentAtAsc(conversation);
        for (Message m : messages) {
            if (!m.getSender().getUserId().equals(reader.getUserId()) && !m.isRead()) {
                m.setRead(true);
            }
        }
        messageRepository.saveAll(messages);
    }

    public long getUnreadCount(User user) {
        if (user == null) return 0;
        return messageRepository.countBySenderNotAndIsReadFalse(user);
    }
}
