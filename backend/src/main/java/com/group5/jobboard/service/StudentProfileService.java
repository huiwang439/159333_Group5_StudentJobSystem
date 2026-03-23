package com.group5.jobboard.service;

import com.group5.jobboard.dto.StudentProfileRequest;

import java.util.Map;

public interface StudentProfileService {

    Map<String, Object> saveProfile(Long userId, StudentProfileRequest request);

    Map<String, Object> getProfile(Long userId);
}