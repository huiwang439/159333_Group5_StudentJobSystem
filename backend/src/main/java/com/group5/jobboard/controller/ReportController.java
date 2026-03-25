package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final JwtUtil jwtUtil;

    public ReportController(ReportService reportService, JwtUtil jwtUtil) {
        this.reportService = reportService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createReport(@RequestParam Long reportedUserId,
                                                         @RequestParam(required = false) Long relatedJobId,
                                                         @RequestParam String reason,
                                                         @RequestParam(required = false) String description,
                                                         HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long reporterUserId = jwtUtil.getUserId(token);

        Map<String, Object> result = reportService.createReport(
                reporterUserId, reportedUserId, relatedJobId, reason, description
        );
        return ApiResponse.success("report submitted", result);
    }

    @GetMapping("/my")
    public ApiResponse<List<Map<String, Object>>> getMyReports(HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long reporterUserId = jwtUtil.getUserId(token);

        List<Map<String, Object>> result = reportService.getMyReports(reporterUserId);
        return ApiResponse.success(result);
    }

    @GetMapping("/admin")
    public ApiResponse<List<Map<String, Object>>> getAllReports(@RequestParam(required = false) String reportStatus,
                                                                HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String role = jwtUtil.getRole(token);

        if (!"admin".equals(role)) {
            throw new RuntimeException("Only admin can view all reports");
        }

        List<Map<String, Object>> result = reportService.getAllReports(reportStatus);
        return ApiResponse.success(result);
    }

    @PutMapping("/admin/{reportId}/handle")
    public ApiResponse<Map<String, Object>> handleReport(@PathVariable Long reportId,
                                                         @RequestParam String reportStatus,
                                                         HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long adminId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"admin".equals(role)) {
            throw new RuntimeException("Only admin can handle reports");
        }

        Map<String, Object> result = reportService.handleReport(reportId, reportStatus, adminId);
        return ApiResponse.success("report handled", result);
    }
}