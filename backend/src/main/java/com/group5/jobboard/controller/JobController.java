package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.dto.JobCreateRequest;
import com.group5.jobboard.service.JobService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;
    private final JwtUtil jwtUtil;

    public JobController(JobService jobService, JwtUtil jwtUtil) {
        this.jobService = jobService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createJob(@Valid @RequestBody JobCreateRequest request,
                                                      HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long employerId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"employer".equals(role)) {
            throw new RuntimeException("Only employer can create job");
        }

        Map<String, Object> result = jobService.createJob(employerId, request);
        return ApiResponse.success("job created", result);
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String fieldOfStudy
    ) {
        boolean hasFilter =
                (keyword != null && !keyword.isBlank()) ||
                        (location != null && !location.isBlank()) ||
                        (employmentType != null && !employmentType.isBlank()) ||
                        (fieldOfStudy != null && !fieldOfStudy.isBlank());

        if (hasFilter) {
            return ApiResponse.success(
                    jobService.searchPublicJobs(keyword, location, employmentType, fieldOfStudy)
            );
        }

        return ApiResponse.success(jobService.getPublicJobs());
    }

    @GetMapping("/{jobId}")
    public ApiResponse<Map<String, Object>> getJobDetail(@PathVariable Long jobId) {
        return ApiResponse.success(jobService.getJobDetail(jobId));
    }

    @GetMapping("/my")
    public ApiResponse<List<Map<String, Object>>> getMyJobs(HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Long employerId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!"employer".equals(role)) {
            throw new RuntimeException("Only employer can view own jobs");
        }

        return ApiResponse.success(jobService.getEmployerJobs(employerId));
    }
}