package com.group5.jobboard.repository;

import com.group5.jobboard.entity.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {

    List<VerificationRequest> findByEmployerProfileId(Long employerProfileId);

    List<VerificationRequest> findByReviewStatus(String reviewStatus);

    Optional<VerificationRequest> findByEmployerProfileIdAndReviewStatus(Long employerProfileId, String reviewStatus);
}