package com.group5.jobboard.repository;

import com.group5.jobboard.entity.AnalyticsLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalyticsLogRepository extends JpaRepository<AnalyticsLog, Long> {

    List<AnalyticsLog> findByUserId(Long userId);

    List<AnalyticsLog> findByActionType(String actionType);
}