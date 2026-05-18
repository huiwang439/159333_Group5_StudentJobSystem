package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.service.StudentDocumentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/students/documents")
public class StudentDocumentController {

    private final StudentDocumentService studentDocumentService;
    private final JwtUtil jwtUtil;

    public StudentDocumentController(StudentDocumentService studentDocumentService,
                                     JwtUtil jwtUtil) {
        this.studentDocumentService = studentDocumentService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> uploadDocument(@RequestParam MultipartFile file,
                                                           @RequestParam String documentType,
                                                           HttpServletRequest request) {
        Long studentId = getUserId(request);
        String role = getRole(request);

        if (!"student".equals(role)) {
            throw new RuntimeException("Only student can upload documents");
        }

        return ApiResponse.success(
                "document uploaded",
                studentDocumentService.uploadDocument(studentId, role, file, documentType)
        );
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getMyDocuments(HttpServletRequest request) {
        Long studentId = getUserId(request);
        String role = getRole(request);

        if (!"student".equals(role)) {
            throw new RuntimeException("Only student can view documents");
        }

        return ApiResponse.success(studentDocumentService.getMyDocuments(studentId, role));
    }

    @PatchMapping("/{id}/default-resume")
    public ApiResponse<Map<String, Object>> setDefaultResume(@PathVariable Long id,
                                                             HttpServletRequest request) {
        Long studentId = getUserId(request);
        String role = getRole(request);

        if (!"student".equals(role)) {
            throw new RuntimeException("Only student can set default resume");
        }

        return ApiResponse.success(
                "default resume updated",
                studentDocumentService.setDefaultResume(studentId, role, id)
        );
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