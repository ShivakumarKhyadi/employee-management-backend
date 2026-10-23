package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FileDTO(
        Long id,
        String filename,
        Long size,
        String type,
        Long employeeId,
        String documentType,
        LocalDate issueDate,
        LocalDate expiryDate,
        String expiryStatus,
        String createdBy,
        LocalDateTime createdDate,
        String lastModifiedBy,
        LocalDateTime lastModifiedDate
) {}