package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.service.EmployerDashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/employers")
public class EmployerDashboardController {

    private final EmployerDashboardService employerDashboardService;
    private final JwtUtil jwtUtil;

    public EmployerDashboardController(EmployerDashboardService employerDashboardService,
                                       JwtUtil jwtUtil) {
        this.employerDashboardService = employerDashboardService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> getDashboard(HttpServletRequest httpServletRequest) {
        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long employerId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"employer".equals(role)) {
            throw new RuntimeException("Only employer can view dashboard");
        }

        return ApiResponse.success(
                employerDashboardService.getDashboard(employerId)
        );
    }
}