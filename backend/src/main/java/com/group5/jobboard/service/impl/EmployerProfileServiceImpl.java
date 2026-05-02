package com.group5.jobboard.service.impl;

import com.group5.jobboard.dto.EmployerProfileRequest;
import com.group5.jobboard.entity.EmployerProfile;
import com.group5.jobboard.repository.EmployerProfileRepository;
import com.group5.jobboard.service.EmployerProfileService;
import com.group5.jobboard.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmployerProfileServiceImpl implements EmployerProfileService {

    private final EmployerProfileRepository employerProfileRepository;
    private final FileStorageService fileStorageService;

    public EmployerProfileServiceImpl(EmployerProfileRepository employerProfileRepository,
                                      FileStorageService fileStorageService) {
        this.employerProfileRepository = employerProfileRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public Map<String, Object> saveProfile(Long userId, EmployerProfileRequest request) {

        EmployerProfile profile = employerProfileRepository.findByUserId(userId)
                .orElse(new EmployerProfile());

        profile.setUserId(userId);
        profile.setCompanyName(request.getCompanyName());
        profile.setIndustry(request.getIndustry());
        profile.setCompanySize(request.getCompanySize());
        profile.setWebsite(request.getWebsite());
        profile.setLocation(request.getLocation());
        profile.setCompanyDescription(request.getCompanyDescription());
        profile.setContactPerson(request.getContactPerson());
        profile.setContactEmail(request.getContactEmail());

        if (profile.getVerificationStatus() == null) {
            profile.setVerificationStatus("pending");
        }

        employerProfileRepository.save(profile);

        Map<String, Object> result = new HashMap<>();
        result.put("employerProfileId", profile.getId());
        result.put("verificationStatus", profile.getVerificationStatus());
        result.put("logoUrl", profile.getLogoUrl());

        return result;
    }

    @Override
    public Map<String, Object> getProfile(Long userId) {
        EmployerProfile profile = employerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employer profile not found"));

        return toMap(profile);
    }

    @Override
    public Map<String, Object> uploadLogo(Long employerId, MultipartFile file) {

        EmployerProfile profile = employerProfileRepository.findByUserId(employerId)
                .orElseThrow(() -> new RuntimeException("Employer profile not found"));

        String logoUrl = fileStorageService.saveFile(file, "employer-logos");

        profile.setLogoUrl(logoUrl);
        employerProfileRepository.save(profile);

        Map<String, Object> result = toMap(profile);
        result.put("updated", true);

        return result;
    }

    private Map<String, Object> toMap(EmployerProfile profile) {
        Map<String, Object> result = new HashMap<>();

        result.put("employerProfileId", profile.getId());
        result.put("userId", profile.getUserId());
        result.put("companyName", profile.getCompanyName());
        result.put("industry", profile.getIndustry());
        result.put("companySize", profile.getCompanySize());
        result.put("website", profile.getWebsite());
        result.put("location", profile.getLocation());
        result.put("companyDescription", profile.getCompanyDescription());
        result.put("contactPerson", profile.getContactPerson());
        result.put("contactEmail", profile.getContactEmail());
        result.put("verificationStatus", profile.getVerificationStatus());
        result.put("logoUrl", profile.getLogoUrl());

        return result;
    }
}