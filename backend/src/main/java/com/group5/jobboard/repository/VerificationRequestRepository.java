package com.group5.jobboard.repository;

import com.group5.jobboard.entity.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {

    Optional<VerificationRequest> findByEmployerProfileId(Long employerProfileId);

    List<VerificationRequest> findByReviewStatus(String reviewStatus);
}