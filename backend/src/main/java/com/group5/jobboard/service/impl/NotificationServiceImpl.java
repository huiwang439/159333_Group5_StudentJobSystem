package com.group5.jobboard.service.impl;

import com.group5.jobboard.entity.Notification;
import com.group5.jobboard.repository.NotificationRepository;
import com.group5.jobboard.service.NotificationService;
import org.springframework.stereotype.Service;

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
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toMap)
                .toList();
    }

    @Override
    public List<Map<String, Object>> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toMap)
                .toList();
    }

    @Override
    public Map<String, Object> getUnreadCount(Long userId) {
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("unreadCount", unreadCount);

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
        Notification saved = notificationRepository.save(notification);

        Map<String, Object> result = toMap(saved);
        result.put("updated", true);
        return result;
    }

    @Override
    public Map<String, Object> markAllAsRead(Long userId) {
        List<Notification> notifications =
                notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

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

        Notification saved = notificationRepository.save(notification);
        return toMap(saved);
    }

    private Map<String, Object> toMap(Notification notification) {
        Map<String, Object> item = new HashMap<>();
        item.put("notificationId", notification.getId());
        item.put("userId", notification.getUserId());
        item.put("notificationType", notification.getNotificationType());
        item.put("title", notification.getTitle());
        item.put("message", notification.getMessage());
        item.put("isRead", notification.getIsRead());
        item.put("createdAt", notification.getCreatedAt());
        return item;
    }
}