package com.group5.jobboard.service;

import java.util.List;
import java.util.Map;

public interface AdminService {

    List<Map<String, Object>> getUsers(String role, String keyword);

    Map<String, Object> getUserDetail(Long userId);

    Map<String, Object> updateUserStatus(Long adminId, Long userId, String status);

    List<Map<String, Object>> getAdminJobs(String status, String keyword);

    Map<String, Object> getAdminJobDetail(Long jobId);

    Map<String, Object> updateJobStatus(Long adminId, Long jobId, String status, String reason);

    Map<String, Object> removeJob(Long adminId, Long jobId);

    List<Map<String, Object>> getEmployers(String industry, String keyword);

    Map<String, Object> getEmployerDetail(Long userId);

    List<Map<String, Object>> getAbnormalUsers(String riskLevel);

    Map<String, Object> getAbnormalUserDetail(Long userId);

    Map<String, Object> getActiveToday();

    List<Map<String, Object>> getHourlyActive();

    List<Map<String, Object>> getTrend(int days);

    Map<String, Object> getDistribution();
}