package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.dto.StudentProfileRequest;
import com.group5.jobboard.service.StudentProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/students")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;
    private final JwtUtil jwtUtil;

    public StudentProfileController(StudentProfileService studentProfileService, JwtUtil jwtUtil) {
        this.studentProfileService = studentProfileService;
        this.jwtUtil = jwtUtil;
    }

    @PutMapping("/profile")
    public ApiResponse<Map<String, Object>> saveProfile(@Valid @RequestBody StudentProfileRequest request,
                                                        HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"student".equals(role)) {
            throw new RuntimeException("Only student can access student profile");
        }

        Map<String, Object> result = studentProfileService.saveProfile(userId, request);
        return ApiResponse.success("student profile saved", result);
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

        if (!"student".equals(role)) {
            throw new RuntimeException("Only student can access student profile");
        }

        Map<String, Object> result = studentProfileService.getProfile(userId);
        return ApiResponse.success(result);
    }
}