package com.group5.jobboard.repository;

import com.group5.jobboard.entity.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentDocumentRepository extends JpaRepository<StudentDocument, Long> {

    List<StudentDocument> findByStudentIdOrderByUploadedAtDesc(Long studentId);

    Optional<StudentDocument> findByIdAndStudentId(Long id, Long studentId);

    List<StudentDocument> findByStudentIdAndDocumentType(Long studentId, String documentType);

    Optional<StudentDocument> findByStudentIdAndDocumentTypeAndDefaultResumeTrue(Long studentId, String documentType);
}