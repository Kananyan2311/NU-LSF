package com.example.nulsf.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.nulsf.model.Notification;
import com.example.nulsf.model.User;
import com.example.nulsf.repository.NotificationRepository;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void notifyUser(User recipient, String title, String message, String link, String type) {
        if (recipient == null) return;
        Notification n = new Notification(recipient, title, message, link, type);
        notificationRepository.save(n);
    }

    public List<Notification> getUserNotifications(User recipient) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
    }

    public long getUnreadCount(User recipient) {
        if (recipient == null) return 0;
        return notificationRepository.countByRecipientAndIsReadFalse(recipient);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    public void markAllAsRead(User recipient) {
        List<Notification> list = notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
        for (Notification n : list) {
            n.setRead(true);
        }
        notificationRepository.saveAll(list);
    }
}
