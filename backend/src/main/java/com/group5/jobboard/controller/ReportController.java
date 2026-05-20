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
                                                         HttpServletRequest request) {

        Long reporterUserId = getUserId(request);

        Map<String, Object> result = reportService.createReport(
                reporterUserId,
                reportedUserId,
                relatedJobId,
                reason,
                description
        );

        return ApiResponse.success("report submitted", result);
    }

    @GetMapping("/my")
    public ApiResponse<List<Map<String, Object>>> getMyReports(HttpServletRequest request) {

        Long reporterUserId = getUserId(request);

        return ApiResponse.success(reportService.getMyReports(reporterUserId));
    }

    @GetMapping("/admin")
    public ApiResponse<List<Map<String, Object>>> getAllReports(@RequestParam(required = false) String status,
                                                                HttpServletRequest request) {

        requireAdminOrStaff(request);

        return ApiResponse.success(reportService.getAllReports(status));
    }

    @PatchMapping("/admin/{reportId}/handle")
    public ApiResponse<Map<String, Object>> handleReport(@PathVariable Long reportId,
                                                         @RequestParam String status,
                                                         HttpServletRequest request) {

        Long operatorId = getUserId(request);

        requireAdminOrStaff(request);

        Map<String, Object> result = reportService.handleReport(reportId, status, operatorId);

        return ApiResponse.success("report handled", result);
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

    private String getRole(HttpServletRequest request) {
        return jwtUtil.getRole(getToken(request));
    }

    private void requireAdminOrStaff(HttpServletRequest request) {
        String role = getRole(request);

        if (!"admin".equals(role) && !"staff".equals(role)) {
            throw new RuntimeException("Only admin or staff can access this API");
        }
    }
}