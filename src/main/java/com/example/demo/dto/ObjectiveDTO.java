package com.example.demo.dto;

import com.example.demo.model.GoalStatus;

import java.util.List;

public record ObjectiveDTO(
        Long id,
        Long employeeId,
        String employeeName,
        String title,
        String description,
        String quarter,
        GoalStatus status,
        List<KeyResultDTO> keyResults,
        double progressPercent
) {}