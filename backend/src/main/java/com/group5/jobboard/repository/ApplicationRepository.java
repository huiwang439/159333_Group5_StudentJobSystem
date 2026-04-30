package com.group5.jobboard.repository;

import com.group5.jobboard.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByStudentId(Long studentId);

    List<Application> findByJobId(Long jobId);

    Optional<Application> findByJobIdAndStudentId(Long jobId, Long studentId);

    List<Application> findByStatus(String status);

    List<Application> findByAppliedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(String status);
}