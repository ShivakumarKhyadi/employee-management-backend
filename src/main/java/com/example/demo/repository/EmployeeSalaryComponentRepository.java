package com.example.demo.repository;

import com.example.demo.model.EmployeeSalaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployeeSalaryComponentRepository extends JpaRepository<EmployeeSalaryComponent, Long> {
    List<EmployeeSalaryComponent> findByEmployeeId(Long employeeId);
    Optional<EmployeeSalaryComponent> findByEmployeeIdAndComponentId(Long employeeId, Long componentId);
}