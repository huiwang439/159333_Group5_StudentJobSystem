package com.group5.jobboard.service;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    List<Map<String, Object>> getMyNotifications(Long userId);

    List<Map<String, Object>> getUnreadNotifications(Long userId);

    Map<String, Object> getUnreadCount(Long userId);

    Map<String, Object> markAsRead(Long notificationId, Long userId);

    Map<String, Object> markAllAsRead(Long userId);

    Map<String, Object> createNotification(Long userId, String type, String title, String message);
}