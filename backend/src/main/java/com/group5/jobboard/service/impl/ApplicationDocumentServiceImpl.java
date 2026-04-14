package com.group5.jobboard.service.impl;

import com.group5.jobboard.entity.Application;
import com.group5.jobboard.entity.ApplicationDocument;
import com.group5.jobboard.entity.Job;
import com.group5.jobboard.repository.ApplicationDocumentRepository;
import com.group5.jobboard.repository.ApplicationRepository;
import com.group5.jobboard.repository.JobRepository;
import com.group5.jobboard.service.ApplicationDocumentService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApplicationDocumentServiceImpl implements ApplicationDocumentService {

    private final ApplicationDocumentRepository applicationDocumentRepository;
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    public ApplicationDocumentServiceImpl(ApplicationDocumentRepository applicationDocumentRepository,
                                          ApplicationRepository applicationRepository,
                                          JobRepository jobRepository) {
        this.applicationDocumentRepository = applicationDocumentRepository;
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public Map<String, Object> uploadDocument(Long userId, String role, Long applicationId,
                                              String documentType, String fileUrl, String fileName) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        boolean allowed =
                "admin".equals(role)
                        || ("student".equals(role) && application.getStudentId().equals(userId))
                        || ("employer".equals(role) && job.getEmployerId().equals(userId));

        if (!allowed) {
            throw new RuntimeException("You are not allowed to upload document for this application");
        }

        ApplicationDocument document = new ApplicationDocument();
        document.setApplicationId(applicationId);
        document.setDocumentType(documentType);
        document.setFileUrl(fileUrl);
        document.setFileName(fileName);

        applicationDocumentRepository.save(document);

        Map<String, Object> result = new HashMap<>();
        result.put("documentId", document.getId());
        result.put("applicationId", document.getApplicationId());
        result.put("documentType", document.getDocumentType());
        result.put("fileUrl", document.getFileUrl());
        result.put("fileName", document.getFileName());

        return result;
    }

    @Override
    public List<Map<String, Object>> getDocumentsByApplication(Long userId, String role, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        boolean allowed =
                "admin".equals(role)
                        || ("student".equals(role) && application.getStudentId().equals(userId))
                        || ("employer".equals(role) && job.getEmployerId().equals(userId));

        if (!allowed) {
            throw new RuntimeException("You are not allowed to view documents for this application");
        }

        List<ApplicationDocument> documents = applicationDocumentRepository.findByApplicationId(applicationId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ApplicationDocument document : documents) {
            Map<String, Object> item = new HashMap<>();
            item.put("documentId", document.getId());
            item.put("applicationId", document.getApplicationId());
            item.put("documentType", document.getDocumentType());
            item.put("fileUrl", document.getFileUrl());
            item.put("fileName", document.getFileName());
            item.put("uploadedAt", document.getUploadedAt());
            result.add(item);
        }

        return result;
    }
}