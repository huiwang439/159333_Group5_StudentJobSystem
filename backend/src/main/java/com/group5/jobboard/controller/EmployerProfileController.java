package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.dto.EmployerProfileRequest;
import com.group5.jobboard.service.EmployerProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/employers")
public class EmployerProfileController {

    private final EmployerProfileService employerProfileService;
    private final JwtUtil jwtUtil;

    public EmployerProfileController(EmployerProfileService employerProfileService, JwtUtil jwtUtil) {
        this.employerProfileService = employerProfileService;
        this.jwtUtil = jwtUtil;
    }

    @PutMapping("/profile")
    public ApiResponse<Map<String, Object>> saveProfile(@Valid @RequestBody EmployerProfileRequest request,
                                                        HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"employer".equals(role)) {
            throw new RuntimeException("Only employer can access employer profile");
        }

        Map<String, Object> result = employerProfileService.saveProfile(userId, request);
        return ApiResponse.success("employer profile saved", result);
    }

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> getProfile(HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"employer".equals(role)) {
            throw new RuntimeException("Only employer can access employer profile");
        }

        Map<String, Object> result = employerProfileService.getProfile(userId);
        return ApiResponse.success(result);
    }
}