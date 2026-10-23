package com.example.demo.dto;

import com.example.demo.model.JobStatus;

import java.time.LocalDate;

public record JobPostingDTO(
        Long id,
        String title,
        Long departmentId,
        String departmentName,
        String description,
        JobStatus status,
        LocalDate postedDate,
        LocalDate closingDate,
        long applicationCount,
        String createdBy,
        java.time.LocalDateTime createdDate
) {}