package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<List<Map<String, Object>>> getMyNotifications(HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);

        List<Map<String, Object>> result = notificationService.getMyNotifications(userId);
        return ApiResponse.success(result);
    }

    @GetMapping("/unread")
    public ApiResponse<List<Map<String, Object>>> getUnreadNotifications(HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);

        List<Map<String, Object>> result = notificationService.getUnreadNotifications(userId);
        return ApiResponse.success(result);
    }

    @PutMapping("/{notificationId}/read")
    public ApiResponse<Map<String, Object>> markAsRead(@PathVariable Long notificationId,
                                                       HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);

        Map<String, Object> result = notificationService.markAsRead(notificationId, userId);
        return ApiResponse.success("notification marked as read", result);
    }

    @PutMapping("/read-all")
    public ApiResponse<Map<String, Object>> markAllAsRead(HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);

        Map<String, Object> result = notificationService.markAllAsRead(userId);
        return ApiResponse.success("all notifications marked as read", result);
    }
}