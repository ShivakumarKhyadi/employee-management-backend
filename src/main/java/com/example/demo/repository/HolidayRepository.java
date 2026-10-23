package com.example.demo.repository;

import com.example.demo.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    List<Holiday> findByDateBetweenOrderByDateAsc(LocalDate start, LocalDate end);
    List<Holiday> findAllByOrderByDateAsc();
}