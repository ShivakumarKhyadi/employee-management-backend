package com.example.demo.dto;

import java.time.LocalDateTime;

public record AcknowledgmentDTO(
        Long id,
        Long fileId,
        String fileName,
        Long employeeId,
        String employeeName,
        String signedName,
        LocalDateTime acknowledgedDate
) {}