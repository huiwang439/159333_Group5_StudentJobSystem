package com.group5.jobboard.service;

import com.group5.jobboard.dto.CareerEventRequest;

import java.util.List;
import java.util.Map;

public interface CareerEventService {

    Map<String, Object> createEvent(Long operatorId, CareerEventRequest request);

    Map<String, Object> updateEvent(Long eventId, CareerEventRequest request);

    Map<String, Object> deleteEvent(Long eventId);

    List<Map<String, Object>> getAllEvents();

    List<Map<String, Object>> getActiveEvents();

    Map<String, Object> getEventDetail(Long eventId);
}