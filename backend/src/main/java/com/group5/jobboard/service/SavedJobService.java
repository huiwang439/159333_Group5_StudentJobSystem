package com.group5.jobboard.service;

import java.util.List;
import java.util.Map;

public interface SavedJobService {

    Map<String, Object> saveJob(Long userId, Long jobId);

    Map<String, Object> removeSavedJob(Long userId, Long jobId);

    List<Map<String, Object>> getMySavedJobs(Long userId);
}