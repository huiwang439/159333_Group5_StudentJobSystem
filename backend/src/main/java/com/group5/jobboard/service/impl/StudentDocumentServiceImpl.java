package com.group5.jobboard.service.impl;

import com.group5.jobboard.entity.StudentDocument;
import com.group5.jobboard.repository.StudentDocumentRepository;
import com.group5.jobboard.service.FileStorageService;
import com.group5.jobboard.service.StudentDocumentService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class StudentDocumentServiceImpl implements StudentDocumentService {

    private final StudentDocumentRepository studentDocumentRepository;
    private final FileStorageService fileStorageService;

    public StudentDocumentServiceImpl(StudentDocumentRepository studentDocumentRepository,
                                      FileStorageService fileStorageService) {
        this.studentDocumentRepository = studentDocumentRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public Map<String, Object> uploadDocument(Long studentId, String role, MultipartFile file, String documentType) {
        if (!"student".equals(role)) {
            throw new RuntimeException("Only student can upload documents");
        }

        if (!"resume".equals(documentType) && !"portfolio".equals(documentType)) {
            throw new RuntimeException("documentType must be resume or portfolio");
        }

        String fileUrl = fileStorageService.saveFile(file, "student-documents");

        StudentDocument document = new StudentDocument();
        document.setStudentId(studentId);
        document.setDocumentType(documentType);
        document.setFileUrl(fileUrl);
        document.setFileName(file.getOriginalFilename());

        if ("resume".equals(documentType)) {
            List<StudentDocument> resumes =
                    studentDocumentRepository.findByStudentIdAndDocumentType(studentId, "resume");
            if (resumes.isEmpty()) {
                document.setDefaultResume(true);
            }
        }

        studentDocumentRepository.save(document);

        return toMap(document);
    }

    @Override
    public List<Map<String, Object>> getMyDocuments(Long studentId, String role) {
        if (!"student".equals(role)) {
            throw new RuntimeException("Only student can view own documents");
        }

        List<StudentDocument> documents =
                studentDocumentRepository.findByStudentIdOrderByUploadedAtDesc(studentId);

        List<Map<String, Object>> result = new ArrayList<>();

        for (StudentDocument document : documents) {
            result.add(toMap(document));
        }

        return result;
    }

    @Override
    public Map<String, Object> setDefaultResume(Long studentId, String role, Long documentId) {
        if (!"student".equals(role)) {
            throw new RuntimeException("Only student can set default resume");
        }

        StudentDocument target = studentDocumentRepository.findByIdAndStudentId(documentId, studentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!"resume".equals(target.getDocumentType())) {
            throw new RuntimeException("Only resume can be set as default");
        }

        List<StudentDocument> resumes =
                studentDocumentRepository.findByStudentIdAndDocumentType(studentId, "resume");

        for (StudentDocument resume : resumes) {
            resume.setDefaultResume(false);
        }

        target.setDefaultResume(true);
        studentDocumentRepository.saveAll(resumes);
        studentDocumentRepository.save(target);

        Map<String, Object> result = toMap(target);
        result.put("defaultUpdated", true);

        return result;
    }

    private Map<String, Object> toMap(StudentDocument document) {
        Map<String, Object> item = new HashMap<>();
        item.put("documentId", document.getId());
        item.put("studentId", document.getStudentId());
        item.put("documentType", document.getDocumentType());
        item.put("fileUrl", document.getFileUrl());
        item.put("fileName", document.getFileName());
        item.put("defaultResume", document.getDefaultResume());
        item.put("uploadedAt", document.getUploadedAt());
        return item;
    }
}