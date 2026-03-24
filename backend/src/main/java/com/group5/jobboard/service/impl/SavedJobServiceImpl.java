package com.group5.jobboard.service.impl;

import com.group5.jobboard.entity.Job;
import com.group5.jobboard.entity.SavedJob;
import com.group5.jobboard.entity.StudentProfile;
import com.group5.jobboard.repository.JobRepository;
import com.group5.jobboard.repository.SavedJobRepository;
import com.group5.jobboard.repository.StudentProfileRepository;
import com.group5.jobboard.service.SavedJobService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SavedJobServiceImpl implements SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final JobRepository jobRepository;

    public SavedJobServiceImpl(SavedJobRepository savedJobRepository,
                               StudentProfileRepository studentProfileRepository,
                               JobRepository jobRepository) {
        this.savedJobRepository = savedJobRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public Map<String, Object> saveJob(Long userId, Long jobId) {

        StudentProfile studentProfile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (savedJobRepository.findByStudentProfileIdAndJobId(studentProfile.getId(), jobId).isPresent()) {
            throw new RuntimeException("You have already saved this job");
        }

        SavedJob savedJob = new SavedJob();
        savedJob.setStudentProfileId(studentProfile.getId());
        savedJob.setJobId(job.getId());

        savedJobRepository.save(savedJob);

        Map<String, Object> result = new HashMap<>();
        result.put("savedJobId", savedJob.getId());
        result.put("studentProfileId", savedJob.getStudentProfileId());
        result.put("jobId", savedJob.getJobId());
        result.put("savedAt", savedJob.getSavedAt());

        return result;
    }

    @Override
    public Map<String, Object> removeSavedJob(Long userId, Long jobId) {

        StudentProfile studentProfile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        SavedJob savedJob = savedJobRepository.findByStudentProfileIdAndJobId(studentProfile.getId(), jobId)
                .orElseThrow(() -> new RuntimeException("Saved job not found"));

        savedJobRepository.delete(savedJob);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Saved job removed successfully");
        result.put("studentProfileId", studentProfile.getId());
        result.put("jobId", jobId);

        return result;
    }

    @Override
    public List<Map<String, Object>> getMySavedJobs(Long userId) {

        StudentProfile studentProfile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        List<SavedJob> savedJobs = savedJobRepository.findByStudentProfileId(studentProfile.getId());
        List<Map<String, Object>> result = new ArrayList<>();

        for (SavedJob savedJob : savedJobs) {
            Map<String, Object> item = new HashMap<>();
            item.put("savedJobId", savedJob.getId());
            item.put("studentProfileId", savedJob.getStudentProfileId());
            item.put("jobId", savedJob.getJobId());
            item.put("savedAt", savedJob.getSavedAt());

            Job job = jobRepository.findById(savedJob.getJobId()).orElse(null);
            if (job != null) {
                item.put("jobTitle", job.getTitle());
                item.put("jobLocation", job.getLocation());
                item.put("jobType", job.getJobType());
                item.put("jobDescription", job.getDescription());
                item.put("jobStatus", job.getStatus());
            }

            result.add(item);
        }

        return result;
    }
}