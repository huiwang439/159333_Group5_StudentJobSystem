package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.service.RecommendationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final JwtUtil jwtUtil;

    public RecommendationController(RecommendationService recommendationService,
                                    JwtUtil jwtUtil) {
        this.recommendationService = recommendationService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/my")
    public ApiResponse<List<Map<String, Object>>> getMyRecommendations(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"student".equals(role)) {
            throw new RuntimeException("Only students can view recommendations");
        }

        return ApiResponse.success(recommendationService.getMyRecommendations(userId));
    }
}