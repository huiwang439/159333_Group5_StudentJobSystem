package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.service.JobCategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
public class JobCategoryController {

    private final JobCategoryService jobCategoryService;
    private final JwtUtil jwtUtil;

    public JobCategoryController(JobCategoryService jobCategoryService, JwtUtil jwtUtil) {
        this.jobCategoryService = jobCategoryService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createCategory(@RequestParam String name,
                                                           HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String role = jwtUtil.getRole(token);

        if (!"admin".equals(role)) {
            throw new RuntimeException("Only admin can create category");
        }

        Map<String, Object> result = jobCategoryService.createCategory(name);
        return ApiResponse.success("category created", result);
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getAllCategories() {
        List<Map<String, Object>> result = jobCategoryService.getAllCategories();
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Map<String, Object>> deleteCategory(@PathVariable Long categoryId,
                                                           HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String role = jwtUtil.getRole(token);

        if (!"admin".equals(role)) {
            throw new RuntimeException("Only admin can delete category");
        }

        Map<String, Object> result = jobCategoryService.deleteCategory(categoryId);
        return ApiResponse.success("category deleted", result);
    }
}