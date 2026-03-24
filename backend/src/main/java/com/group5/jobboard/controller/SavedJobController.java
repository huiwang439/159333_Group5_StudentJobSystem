package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.service.SavedJobService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/saved-jobs")
public class SavedJobController {

    private final SavedJobService savedJobService;
    private final JwtUtil jwtUtil;

    public SavedJobController(SavedJobService savedJobService, JwtUtil jwtUtil) {
        this.savedJobService = savedJobService;
        this.jwtUtil = jwtUtil;
    }

    // ⭐ 收藏岗位
    @PostMapping
    public ApiResponse<Map<String, Object>> saveJob(@RequestParam Long jobId,
                                                    HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"student".equals(role)) {
            throw new RuntimeException("Only student can save jobs");
        }

        Map<String, Object> result = savedJobService.saveJob(userId, jobId);
        return ApiResponse.success("job saved", result);
    }

    // ⭐ 取消收藏
    @DeleteMapping
    public ApiResponse<Map<String, Object>> removeSavedJob(@RequestParam Long jobId,
                                                           HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"student".equals(role)) {
            throw new RuntimeException("Only student can remove saved jobs");
        }

        Map<String, Object> result = savedJobService.removeSavedJob(userId, jobId);
        return ApiResponse.success("saved job removed", result);
    }

    // ⭐ 查看我的收藏
    @GetMapping("/my")
    public ApiResponse<List<Map<String, Object>>> getMySavedJobs(HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"student".equals(role)) {
            throw new RuntimeException("Only student can view saved jobs");
        }

        return ApiResponse.success(savedJobService.getMySavedJobs(userId));
    }
}