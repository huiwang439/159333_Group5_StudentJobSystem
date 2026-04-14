package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.service.ApplicationDocumentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/application-documents")
public class ApplicationDocumentController {

    private final ApplicationDocumentService applicationDocumentService;
    private final JwtUtil jwtUtil;

    public ApplicationDocumentController(ApplicationDocumentService applicationDocumentService, JwtUtil jwtUtil) {
        this.applicationDocumentService = applicationDocumentService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> uploadDocument(@RequestParam Long applicationId,
                                                           @RequestParam String documentType,
                                                           @RequestParam String fileUrl,
                                                           @RequestParam(required = false) String fileName,
                                                           HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        Map<String, Object> result = applicationDocumentService.uploadDocument(
                userId, role, applicationId, documentType, fileUrl, fileName
        );
        return ApiResponse.success("document uploaded", result);
    }

    @GetMapping("/{applicationId}")
    public ApiResponse<List<Map<String, Object>>> getDocuments(@PathVariable Long applicationId,
                                                               HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        return ApiResponse.success(
                applicationDocumentService.getDocumentsByApplication(userId, role, applicationId)
        );
    }
}