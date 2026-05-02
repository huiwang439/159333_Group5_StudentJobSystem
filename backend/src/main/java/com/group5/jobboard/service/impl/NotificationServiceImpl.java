package com.group5.jobboard.service.impl;

import com.group5.jobboard.entity.Notification;
import com.group5.jobboard.repository.NotificationRepository;
import com.group5.jobboard.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<Map<String, Object>> getMyNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Notification notification : notifications) {
            Map<String, Object> item = new HashMap<>();
            item.put("notificationId", notification.getId());
            item.put("userId", notification.getUserId());
            item.put("notificationType", notification.getNotificationType());
            item.put("title", notification.getTitle());
            item.put("message", notification.getMessage());
            item.put("isRead", notification.getIsRead());
            item.put("createdAt", notification.getCreatedAt());
            result.add(item);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Notification notification : notifications) {
            Map<String, Object> item = new HashMap<>();
            item.put("notificationId", notification.getId());
            item.put("userId", notification.getUserId());
            item.put("notificationType", notification.getNotificationType());
            item.put("title", notification.getTitle());
            item.put("message", notification.getMessage());
            item.put("isRead", notification.getIsRead());
            item.put("createdAt", notification.getCreatedAt());
            result.add(item);
        }

        return result;
    }

    @Override
    public Map<String, Object> markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("You are not allowed to update this notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);

        Map<String, Object> result = new HashMap<>();
        result.put("notificationId", notification.getId());
        result.put("isRead", notification.getIsRead());
        result.put("updated", true);

        return result;
    }

    @Override
    public Map<String, Object> markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }

        notificationRepository.saveAll(notifications);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("updatedCount", notifications.size());
        result.put("updated", true);

        return result;
    }

    @Override
    public Map<String, Object> createNotification(Long userId, String type, String title, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setIsRead(false);

        notificationRepository.save(notification);

        Map<String, Object> result = new HashMap<>();
        result.put("notificationId", notification.getId());
        result.put("userId", notification.getUserId());
        result.put("notificationType", notification.getNotificationType());
        result.put("title", notification.getTitle());
        result.put("message", notification.getMessage());
        result.put("isRead", notification.getIsRead());
        result.put("createdAt", notification.getCreatedAt());

        return result;
    }
}