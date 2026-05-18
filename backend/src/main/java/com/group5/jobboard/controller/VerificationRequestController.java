package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.service.VerificationRequestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
                                                          HttpServletRequest request) {
        Long userId = getUserId(request);
        String role = getRole(request);

        if (!"employer".equals(role)) {
            throw new RuntimeException("Only employer can submit verification request");
        }

        Map<String, Object> result = verificationRequestService.submitRequest(
                userId,
                businessLicenseUrl,
                supportingDocumentUrl
        );

        return ApiResponse.success("verification request submitted", result);
    }

    @PostMapping("/submit-file")
    public ApiResponse<Map<String, Object>> submitRequestByFile(@RequestParam MultipartFile businessLicenseFile,
                                                                @RequestParam(required = false) MultipartFile supportingDocumentFile,
                                                                HttpServletRequest request) {
        Long userId = getUserId(request);
        String role = getRole(request);

        if (!"employer".equals(role)) {
            throw new RuntimeException("Only employer can submit verification request");
        }

        Map<String, Object> result = verificationRequestService.submitRequestByFile(
                userId,
                businessLicenseFile,
                supportingDocumentFile
        );

        return ApiResponse.success("verification files submitted", result);
    }

    @GetMapping("/my")
    public ApiResponse<Map<String, Object>> getMyRequest(HttpServletRequest request) {
        Long userId = getUserId(request);
        String role = getRole(request);

        if (!"employer".equals(role)) {
            throw new RuntimeException("Only employer can view own verification request");
        }

        return ApiResponse.success(verificationRequestService.getMyRequest(userId));
    }

    @GetMapping("/admin")
    public ApiResponse<List<Map<String, Object>>> getAllRequests(@RequestParam(required = false) String reviewStatus,
                                                                 HttpServletRequest request) {
        String role = getRole(request);

        if (!"admin".equals(role) && !"staff".equals(role)) {
            throw new RuntimeException("Only admin or staff can view verification requests");
        }

        return ApiResponse.success(verificationRequestService.getAllRequests(reviewStatus));
    }

    @PutMapping("/admin/{verificationRequestId}/review")
    public ApiResponse<Map<String, Object>> reviewRequest(@PathVariable Long verificationRequestId,
                                                          @RequestParam String reviewStatus,
                                                          @RequestParam(required = false) String reviewNote,
                                                          HttpServletRequest request) {
        Long reviewerId = getUserId(request);
        String role = getRole(request);

        if (!"admin".equals(role) && !"staff".equals(role)) {
            throw new RuntimeException("Only admin or staff can review verification requests");
        }

        Map<String, Object> result = verificationRequestService.reviewRequest(
                verificationRequestId,
                reviewerId,
                reviewStatus,
                reviewNote
        );

        return ApiResponse.success("verification request reviewed", result);
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
}