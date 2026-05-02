package com.group5.jobboard.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface StudentDocumentService {

    Map<String, Object> uploadDocument(Long studentId, String role, MultipartFile file, String documentType);

    List<Map<String, Object>> getMyDocuments(Long studentId, String role);

    Map<String, Object> setDefaultResume(Long studentId, String role, Long documentId);
}