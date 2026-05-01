package com.group5.jobboard.service;

import java.util.List;
import java.util.Map;

public interface RecommendationService {

    List<Map<String, Object>> getMyRecommendations(Long userId);

}