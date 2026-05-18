package com.group5.jobboard.repository;

import com.group5.jobboard.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByEmployerId(Long employerId);

    List<Job> findByEmployerIdAndStatus(Long employerId, String status);

    List<Job> findByStatus(String status);

    List<Job> findByStatusOrderByCreatedAtDesc(String status);

    List<Job> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(String status);

    List<Job> findByTitleContainingIgnoreCaseOrLocationContainingIgnoreCase(String title, String location);

    List<Job> findByStatusAndTitleContainingIgnoreCaseOrStatusAndLocationContainingIgnoreCase(
            String status1,
            String title,
            String status2,
            String location
    );

    List<Job> findByStatusAndTargetStudentTypeInOrderByCreatedAtDesc(String status, List<String> targetStudentTypes);

    List<Job> findByStatusAndTargetStudentTypeOrderByCreatedAtDesc(String status, String targetStudentType);

    long countByEmployerId(Long employerId);

    long countByEmployerIdAndStatus(Long employerId, String status);
}