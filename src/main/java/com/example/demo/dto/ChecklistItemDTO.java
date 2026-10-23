package com.example.demo.dto;

import com.example.demo.model.ChecklistCategory;

import java.time.LocalDateTime;

public record ChecklistItemDTO(
        Long id,
        Long employeeId,
        String employeeName,
        ChecklistCategory category,
        String taskName,
        boolean completed,
        LocalDateTime completedDate
) {}