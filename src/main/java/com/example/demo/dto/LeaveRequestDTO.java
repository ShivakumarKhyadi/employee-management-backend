package com.example.demo.dto;

import com.example.demo.model.LeaveStatus;
import com.example.demo.model.LeaveType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveRequestDTO(
        Long id,
        Long employeeId,
        String employeeName,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        long dayCount,
        String reason,
        LeaveStatus status,
        LocalDateTime appliedDate,
        String decidedBy,
        LocalDateTime decidedDate,
        String decisionNote
) {}