package com.group5.jobboard.service.impl;

import com.group5.jobboard.dto.StudentProfileRequest;
import com.group5.jobboard.entity.StudentProfile;
import com.group5.jobboard.repository.StudentProfileRepository;
import com.group5.jobboard.service.StudentProfileService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StudentProfileServiceImpl implements StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;

    public StudentProfileServiceImpl(StudentProfileRepository studentProfileRepository) {
        this.studentProfileRepository = studentProfileRepository;
    }

    @Override
    public Map<String, Object> saveProfile(Long userId, StudentProfileRequest request) {

        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElse(new StudentProfile());

        profile.setUserId(userId);
        profile.setUniversity(request.getUniversity());
        profile.setMajor(request.getMajor());
        profile.setDegreeLevel(request.getDegreeLevel());
        profile.setGraduationYear(request.getGraduationYear());
        profile.setSkills(request.getSkills());
        profile.setBio(request.getBio());
        profile.setPreferredLocation(request.getPreferredLocation());
        profile.setPreferredJobType(request.getPreferredJobType());

        String studentType = normalizeStudentType(request.getStudentType());
        profile.setStudentType(studentType);

        StudentProfile saved = studentProfileRepository.save(profile);

        Map<String, Object> result = toMap(saved);
        result.put("updated", true);

        return result;
    }

    @Override
    public Map<String, Object> getProfile(Long userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        return toMap(profile);
    }

    private String normalizeStudentType(String studentType) {
        if (studentType == null || studentType.isBlank()) {
            return "UNDERGRADUATE";
        }

        String value = studentType.trim().toUpperCase();

        if (!"UNDERGRADUATE".equals(value)
                && !"GRADUATE".equals(value)
                && !"RECENT_GRADUATE".equals(value)) {
            throw new RuntimeException("Invalid studentType. Allowed values: UNDERGRADUATE, GRADUATE, RECENT_GRADUATE");
        }

        return value;
    }

    private Map<String, Object> toMap(StudentProfile profile) {
        Map<String, Object> result = new HashMap<>();
        result.put("studentProfileId", profile.getId());
        result.put("userId", profile.getUserId());
        result.put("university", profile.getUniversity());
        result.put("major", profile.getMajor());
        result.put("degreeLevel", profile.getDegreeLevel());
        result.put("graduationYear", profile.getGraduationYear());
        result.put("skills", profile.getSkills());
        result.put("bio", profile.getBio());
        result.put("preferredLocation", profile.getPreferredLocation());
        result.put("preferredJobType", profile.getPreferredJobType());
        result.put("studentType", profile.getStudentType());
        result.put("createdAt", profile.getCreatedAt());
        result.put("updatedAt", profile.getUpdatedAt());
        return result;
    }
}