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
        String token = getToken(request);
        Long studentId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        return ApiResponse.success(
                "document uploaded",
                studentDocumentService.uploadDocument(studentId, role, file, documentType)
        );
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getMyDocuments(HttpServletRequest request) {
        String token = getToken(request);
        Long studentId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        return ApiResponse.success(studentDocumentService.getMyDocuments(studentId, role));
    }

    @PatchMapping("/{id}/default-resume")
    public ApiResponse<Map<String, Object>> setDefaultResume(@PathVariable Long id,
                                                             HttpServletRequest request) {
        String token = getToken(request);
        Long studentId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

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
}