package com.group5.jobboard.service;

import java.util.List;
import java.util.Map;

public interface ApplicationDocumentService {

    Map<String, Object> uploadDocument(Long userId, String role, Long applicationId,
                                       String documentType, String fileUrl, String fileName);

    List<Map<String, Object>> getDocumentsByApplication(Long userId, String role, Long applicationId);
}