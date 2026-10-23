package com.example.demo.repository;

import com.example.demo.model.Objective;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ObjectiveRepository extends JpaRepository<Objective, Long> {
    List<Objective> findByEmployeeIdOrderByQuarterDesc(Long employeeId);
}