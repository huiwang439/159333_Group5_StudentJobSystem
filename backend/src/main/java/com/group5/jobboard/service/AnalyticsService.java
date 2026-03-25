package com.group5.jobboard.service;

import java.util.Map;

public interface AnalyticsService {

    Map<String, Object> getDashboardSummary();

    Map<String, Object> getJobStatistics();

    Map<String, Object> getApplicationStatistics();

    Map<String, Object> getUserStatistics();
}