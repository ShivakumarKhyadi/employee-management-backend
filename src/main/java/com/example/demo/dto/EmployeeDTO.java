package com.example.demo.dto;

import com.example.demo.model.Employee;
import com.example.demo.model.EmployeeType;
import com.example.demo.model.Grade;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeDTO(
        Long empId,
        String name,
        String email,
        Long departmentId,
        String departmentName,
        Double salary,
        LocalDate joiningDate,
        LocalDate dateOfBirth,
        boolean supervisor,
        EmployeeType employeeType,
        Grade grade,
        String designation,
        String photoFilename,
        String projectManagerEmail,
        String loginUsername,
        Long reportsToEmpId,
        String reportsToName,
        String createdBy,
        LocalDateTime createdDate,
        String lastModifiedBy,
        LocalDateTime lastModifiedDate
) {}