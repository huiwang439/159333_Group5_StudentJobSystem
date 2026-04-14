package com.group5.jobboard.repository;

import com.group5.jobboard.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByEmployerId(Long employerId);

    List<Job> findByStatus(String status);

    List<Job> findByStatusAndTitleContainingIgnoreCase(String status, String keyword);

    List<Job> findByStatusAndLocationContainingIgnoreCase(String status, String location);

    List<Job> findByStatusAndEmploymentTypeContainingIgnoreCase(String status, String employmentType);

    List<Job> findByStatusAndFieldOfStudyContainingIgnoreCase(String status, String fieldOfStudy);

    List<Job> findByStatusAndTitleContainingIgnoreCaseAndLocationContainingIgnoreCaseAndEmploymentTypeContainingIgnoreCaseAndFieldOfStudyContainingIgnoreCase(
            String status,
            String keyword,
            String location,
            String employmentType,
            String fieldOfStudy
    );
}