package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.dto.CareerEventRequest;
import com.group5.jobboard.service.CareerEventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/career-events")
public class CareerEventController {

    private final CareerEventService careerEventService;
    private final JwtUtil jwtUtil;

    public CareerEventController(CareerEventService careerEventService, JwtUtil jwtUtil) {
        this.careerEventService = careerEventService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createEvent(
            @Valid @RequestBody CareerEventRequest request,
            HttpServletRequest httpServletRequest
    ) {
        Long operatorId = getUserId(httpServletRequest);
        requireAdminOrStaff(httpServletRequest);

        return ApiResponse.success("career event created", careerEventService.createEvent(operatorId, request));
    }

    @PutMapping("/{eventId}")
    public ApiResponse<Map<String, Object>> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody CareerEventRequest request,
            HttpServletRequest httpServletRequest
    ) {
        requireAdminOrStaff(httpServletRequest);

        return ApiResponse.success("career event updated", careerEventService.updateEvent(eventId, request));
    }

    @DeleteMapping("/{eventId}")
    public ApiResponse<Map<String, Object>> deleteEvent(
            @PathVariable Long eventId,
            HttpServletRequest httpServletRequest
    ) {
        requireAdminOrStaff(httpServletRequest);

        return ApiResponse.success("career event deleted", careerEventService.deleteEvent(eventId));
    }

    @GetMapping("/admin")
    public ApiResponse<List<Map<String, Object>>> getAllEvents(
            HttpServletRequest httpServletRequest
    ) {
        requireAdminOrStaff(httpServletRequest);

        return ApiResponse.success(careerEventService.getAllEvents());
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getActiveEvents() {
        return ApiResponse.success(careerEventService.getActiveEvents());
    }

    @GetMapping("/{eventId}")
    public ApiResponse<Map<String, Object>> getEventDetail(@PathVariable Long eventId) {
        return ApiResponse.success(careerEventService.getEventDetail(eventId));
    }

    private String getToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        return authHeader.substring(7);
    }

    private Long getUserId(HttpServletRequest request) {
        return jwtUtil.getUserId(getToken(request));
    }

    private String getRole(HttpServletRequest request) {
        return jwtUtil.getRole(getToken(request));
    }

    private void requireAdminOrStaff(HttpServletRequest request) {
        String role = getRole(request);

        if (!"admin".equals(role) && !"staff".equals(role)) {
            throw new RuntimeException("Only admin or staff can manage career events");
        }
    }
}