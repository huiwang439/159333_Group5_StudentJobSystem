package com.group5.jobboard.repository;

import com.group5.jobboard.entity.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {

    Optional<JobCategory> findByCategoryName(String categoryName);
}