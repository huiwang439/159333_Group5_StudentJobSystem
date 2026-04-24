package com.group5.jobboard.repository;

import com.group5.jobboard.entity.EmployerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployerProfileRepository extends JpaRepository<EmployerProfile, Long> {

    Optional<EmployerProfile> findByUserId(Long userId);

    List<EmployerProfile> findByIndustry(String industry);

    List<EmployerProfile> findByCompanyNameContainingIgnoreCaseOrIndustryContainingIgnoreCaseOrLocationContainingIgnoreCase(
            String companyName,
            String industry,
            String location
    );

    List<EmployerProfile> findByIndustryAndCompanyNameContainingIgnoreCaseOrIndustryAndLocationContainingIgnoreCase(
            String industry1,
            String companyName,
            String industry2,
            String location
    );
}