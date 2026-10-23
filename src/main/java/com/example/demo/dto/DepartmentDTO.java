package com.example.demo.dto;

import com.example.demo.model.DepartmentStatus;

import java.time.LocalDateTime;

public record DepartmentDTO(
        Long deptId,
        String departmentCode,
        String departmentName,
        DepartmentStatus status,
        String createdBy,
        LocalDateTime createdDate,
        String lastModifiedBy,
        LocalDateTime lastModifiedDate
) {}