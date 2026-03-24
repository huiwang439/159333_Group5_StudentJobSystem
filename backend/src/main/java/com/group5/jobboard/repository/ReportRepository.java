package com.group5.jobboard.repository;

import com.group5.jobboard.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByReporterUserId(Long reporterUserId);

    List<Report> findByReportedUserId(Long reportedUserId);

    List<Report> findByReportStatus(String reportStatus);
}