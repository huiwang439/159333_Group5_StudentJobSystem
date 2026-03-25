package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AnalyticsService analyticsService;
    private final JwtUtil jwtUtil;

    public AdminController(AnalyticsService analyticsService, JwtUtil jwtUtil) {
        this.analyticsService = analyticsService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> getDashboard(HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String role = jwtUtil.getRole(token);

        if (!"admin".equals(role)) {
            throw new RuntimeException("Only admin can access dashboard");
        }

        Map<String, Object> result = analyticsService.getDashboardSummary();
        return ApiResponse.success(result);
    }

    @GetMapping("/dashboard/jobs")
    public ApiResponse<Map<String, Object>> getJobStatistics(HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String role = jwtUtil.getRole(token);

        if (!"admin".equals(role)) {
            throw new RuntimeException("Only admin can access job statistics");
        }

        Map<String, Object> result = analyticsService.getJobStatistics();
        return ApiResponse.success(result);
    }

    @GetMapping("/dashboard/applications")
    public ApiResponse<Map<String, Object>> getApplicationStatistics(HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String role = jwtUtil.getRole(token);

        if (!"admin".equals(role)) {
            throw new RuntimeException("Only admin can access application statistics");
        }

        Map<String, Object> result = analyticsService.getApplicationStatistics();
        return ApiResponse.success(result);
    }

    @GetMapping("/dashboard/users")
    public ApiResponse<Map<String, Object>> getUserStatistics(HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String role = jwtUtil.getRole(token);

        if (!"admin".equals(role)) {
            throw new RuntimeException("Only admin can access user statistics");
        }

        Map<String, Object> result = analyticsService.getUserStatistics();
        return ApiResponse.success(result);
    }
}