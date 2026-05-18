package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    public NotificationController(NotificationService notificationService, JwtUtil jwtUtil) {
        this.notificationService = notificationService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getMyNotifications(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ApiResponse.success(notificationService.getMyNotifications(userId));
    }

    @GetMapping("/unread")
    public ApiResponse<List<Map<String, Object>>> getUnreadNotifications(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ApiResponse.success(notificationService.getUnreadNotifications(userId));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserId(request);

        List<Map<String, Object>> unreadList = notificationService.getUnreadNotifications(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", unreadList.size());

        return ApiResponse.success(result);
    }

    @PutMapping("/{notificationId}/read")
    public ApiResponse<Map<String, Object>> markAsRead(@PathVariable Long notificationId,
                                                       HttpServletRequest request) {
        Long userId = getUserId(request);
        return ApiResponse.success(
                "notification marked as read",
                notificationService.markAsRead(notificationId, userId)
        );
    }

    @PutMapping("/read-all")
    public ApiResponse<Map<String, Object>> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ApiResponse.success(
                "all notifications marked as read",
                notificationService.markAllAsRead(userId)
        );
    }

    private String getToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        return authHeader.substring(7);
    }

    private Long getUserId(HttpServletRequest request) {
        return jwtUtil.getUserId(getToken(request));
    }
}