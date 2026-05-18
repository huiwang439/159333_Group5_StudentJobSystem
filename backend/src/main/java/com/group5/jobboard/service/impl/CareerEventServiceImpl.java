package com.group5.jobboard.service.impl;

import com.group5.jobboard.dto.CareerEventRequest;
import com.group5.jobboard.entity.CareerEvent;
import com.group5.jobboard.repository.CareerEventRepository;
import com.group5.jobboard.service.CareerEventService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CareerEventServiceImpl implements CareerEventService {

    private final CareerEventRepository careerEventRepository;

    public CareerEventServiceImpl(CareerEventRepository careerEventRepository) {
        this.careerEventRepository = careerEventRepository;
    }

    @Override
    public Map<String, Object> createEvent(Long operatorId, CareerEventRequest request) {
        CareerEvent event = new CareerEvent();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setEventDate(request.getEventDate());
        event.setOrganizer(request.getOrganizer());
        event.setStatus("active");
        event.setCreatedBy(operatorId);

        CareerEvent saved = careerEventRepository.save(event);
        return toMap(saved);
    }

    @Override
    public Map<String, Object> updateEvent(Long eventId, CareerEventRequest request) {
        CareerEvent event = careerEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Career event not found"));

        if ("deleted".equals(event.getStatus())) {
            throw new RuntimeException("Career event has been deleted");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setEventDate(request.getEventDate());
        event.setOrganizer(request.getOrganizer());

        CareerEvent saved = careerEventRepository.save(event);
        return toMap(saved);
    }

    @Override
    public Map<String, Object> deleteEvent(Long eventId) {
        CareerEvent event = careerEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Career event not found"));

        event.setStatus("deleted");
        CareerEvent saved = careerEventRepository.save(event);

        Map<String, Object> result = toMap(saved);
        result.put("deleted", true);
        return result;
    }

    @Override
    public List<Map<String, Object>> getAllEvents() {
        return careerEventRepository.findAll()
                .stream()
                .map(this::toMap)
                .toList();
    }

    @Override
    public List<Map<String, Object>> getActiveEvents() {
        return careerEventRepository.findByStatusOrderByEventDateAsc("active")
                .stream()
                .map(this::toMap)
                .toList();
    }

    @Override
    public Map<String, Object> getEventDetail(Long eventId) {
        CareerEvent event = careerEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Career event not found"));

        if ("deleted".equals(event.getStatus())) {
            throw new RuntimeException("Career event has been deleted");
        }

        return toMap(event);
    }

    private Map<String, Object> toMap(CareerEvent event) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", event.getId());
        map.put("title", event.getTitle());
        map.put("description", event.getDescription());
        map.put("location", event.getLocation());
        map.put("eventDate", event.getEventDate());
        map.put("organizer", event.getOrganizer());
        map.put("status", event.getStatus());
        map.put("createdBy", event.getCreatedBy());
        map.put("createdAt", event.getCreatedAt());
        map.put("updatedAt", event.getUpdatedAt());
        return map;
    }
}