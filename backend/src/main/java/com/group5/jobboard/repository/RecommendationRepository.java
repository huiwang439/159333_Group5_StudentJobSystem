package com.group5.jobboard.repository;

import com.group5.jobboard.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findByStudentProfileIdOrderByMatchScoreDesc(Long studentProfileId);

    Optional<Recommendation> findByStudentProfileIdAndJobId(Long studentProfileId, Long jobId);

    void deleteByStudentProfileId(Long studentProfileId);
}