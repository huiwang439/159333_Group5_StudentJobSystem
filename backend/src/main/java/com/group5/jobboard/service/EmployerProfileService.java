package com.group5.jobboard.service;

import com.group5.jobboard.dto.EmployerProfileRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface EmployerProfileService {

    Map<String, Object> saveProfile(Long userId, EmployerProfileRequest request);

    Map<String, Object> getProfile(Long userId);

    Map<String, Object> uploadLogo(Long employerId, MultipartFile file);
}