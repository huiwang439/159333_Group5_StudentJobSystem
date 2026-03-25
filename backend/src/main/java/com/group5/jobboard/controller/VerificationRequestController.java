package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.service.VerificationRequestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/verification")
public class VerificationRequestController {

    private final VerificationRequestService verificationRequestService;
    private final JwtUtil jwtUtil;

    public VerificationRequestController(VerificationRequestService verificationRequestService, JwtUtil jwtUtil) {
        this.verificationRequestService = verificationRequestService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/submit")
    public ApiResponse<Map<String, Object>> submitRequest(@RequestParam String businessLicenseUrl,
                                                          @RequestParam(required = false) String supportingDocumentUrl,
                                                          HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"employer".equals(role)) {
            throw new RuntimeException("Only employer can submit verification request");
        }

        Map<String, Object> result = verificationRequestService.submitRequest(userId, businessLicenseUrl, supportingDocumentUrl);
        return ApiResponse.success("verification request submitted", result);
    }

    @GetMapping("/my")
    public ApiResponse<Map<String, Object>> getMyRequest(HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"employer".equals(role)) {
            throw new RuntimeException("Only employer can view own verification request");
        }

        Map<String, Object> result = verificationRequestService.getMyRequest(userId);
        return ApiResponse.success(result);
    }

    @GetMapping("/admin")
    public ApiResponse<List<Map<String, Object>>> getAllRequests(@RequestParam(required = false) String reviewStatus,
                                                                 HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String role = jwtUtil.getRole(token);

        if (!"admin".equals(role)) {
            throw new RuntimeException("Only admin can view all verification requests");
        }

        List<Map<String, Object>> result = verificationRequestService.getAllRequests(reviewStatus);
        return ApiResponse.success(result);
    }

    @PutMapping("/admin/{verificationRequestId}/review")
    public ApiResponse<Map<String, Object>> reviewRequest(@PathVariable Long verificationRequestId,
                                                          @RequestParam String reviewStatus,
                                                          @RequestParam(required = false) String reviewNote,
                                                          HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long adminId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"admin".equals(role)) {
            throw new RuntimeException("Only admin can review verification requests");
        }

        Map<String, Object> result = verificationRequestService.reviewRequest(
                verificationRequestId, adminId, reviewStatus, reviewNote
        );
        return ApiResponse.success("verification request reviewed", result);
    }
}