package com.example.demo.dto;

public record PendingAcknowledgmentDTO(
        Long fileId,
        String fileName,
        Long employeeId
) {}