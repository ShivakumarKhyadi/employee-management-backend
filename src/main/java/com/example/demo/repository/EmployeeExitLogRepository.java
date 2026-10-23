package com.example.demo.repository;

import com.example.demo.model.EmployeeExitLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeExitLogRepository extends JpaRepository<EmployeeExitLog, Long> {
    List<EmployeeExitLog> findByExitDateAfter(LocalDate date);
}
