package com.group5.jobboard.repository;

import com.group5.jobboard.entity.AnalyticsLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsLogRepository extends JpaRepository<AnalyticsLog, Long> {

    List<AnalyticsLog> findByUserId(Long userId);

    List<AnalyticsLog> findByActionType(String actionType);

    List<AnalyticsLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<AnalyticsLog> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<AnalyticsLog> findByActionTypeAndCreatedAtBetween(String actionType, LocalDateTime start, LocalDateTime end);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}