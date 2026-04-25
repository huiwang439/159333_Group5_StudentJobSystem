package com.group5.jobboard.service;

import java.util.Map;

public interface EmployerDashboardService {

    Map<String, Object> getDashboard(Long employerId);
}