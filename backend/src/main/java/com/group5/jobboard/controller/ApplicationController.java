package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.dto.ApplicationCreateRequest;
import com.group5.jobboard.dto.ApplicationStatusUpdateRequest;
import com.group5.jobboard.service.ApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final JwtUtil jwtUtil;

    public ApplicationController(ApplicationService applicationService, JwtUtil jwtUtil) {
        this.applicationService = applicationService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> submitApplication(
            @Valid @RequestBody ApplicationCreateRequest request,
            HttpServletRequest httpServletRequest) {

        String token = getToken(httpServletRequest);
        Long studentId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"student".equals(role)) {
            throw new RuntimeException("Only student can submit application");
        }

        Map<String, Object> result = applicationService.submitApplication(studentId, request);
        return ApiResponse.success("application submitted", result);
    }

    @GetMapping("/my")
    public ApiResponse<List<Map<String, Object>>> getMyApplications(
            HttpServletRequest httpServletRequest) {

        String token = getToken(httpServletRequest);
        Long studentId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"student".equals(role)) {
            throw new RuntimeException("Only student can view own applications");
        }

        return ApiResponse.success(applicationService.getMyApplications(studentId));
    }

    @GetMapping("/job/{jobId}")
    public ApiResponse<List<Map<String, Object>>> getApplicationsByJob(
            @PathVariable Long jobId,
            @RequestParam(required = false) String status,
            HttpServletRequest httpServletRequest) {

        String token = getToken(httpServletRequest);
        Long employerId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"employer".equals(role)) {
            throw new RuntimeException("Only employer can view applications for a job");
        }

        return ApiResponse.success(applicationService.getApplicationsByJob(employerId, jobId, status));
    }

    @GetMapping("/{applicationId}")
    public ApiResponse<Map<String, Object>> getApplicationDetail(
            @PathVariable Long applicationId,
            HttpServletRequest httpServletRequest) {

        String token = getToken(httpServletRequest);
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        return ApiResponse.success(applicationService.getApplicationDetail(userId, role, applicationId));
    }

    @PatchMapping("/{applicationId}/status")
    public ApiResponse<Map<String, Object>> updateApplicationStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationStatusUpdateRequest request,
            HttpServletRequest httpServletRequest) {

        String token = getToken(httpServletRequest);
        Long employerId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"employer".equals(role)) {
            throw new RuntimeException("Only employer can update application status");
        }

        Map<String, Object> result = applicationService.updateApplicationStatus(employerId, applicationId, request);
        return ApiResponse.success("application status updated", result);
    }

    @GetMapping("/{applicationId}/resume")
    public ApiResponse<Map<String, Object>> getApplicationResume(
            @PathVariable Long applicationId,
            HttpServletRequest httpServletRequest) {

        String token = getToken(httpServletRequest);
        Long employerId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        Map<String, Object> result = applicationService.getApplicationResume(
                employerId,
                role,
                applicationId
        );

        return ApiResponse.success(result);
    }

    private String getToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        return authHeader.substring(7);
    }
}