package com.group5.jobboard.repository;

import com.group5.jobboard.entity.CareerEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerEventRepository extends JpaRepository<CareerEvent, Long> {

    List<CareerEvent> findByStatusOrderByEventDateAsc(String status);
}