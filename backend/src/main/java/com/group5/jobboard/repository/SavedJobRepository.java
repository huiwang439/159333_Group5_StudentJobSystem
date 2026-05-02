package com.group5.jobboard.repository;

import com.group5.jobboard.entity.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    List<SavedJob> findByStudentProfileId(Long studentProfileId);

    Optional<SavedJob> findByStudentProfileIdAndJobId(Long studentProfileId, Long jobId);

    void deleteByStudentProfileIdAndJobId(Long studentProfileId, Long jobId);
}