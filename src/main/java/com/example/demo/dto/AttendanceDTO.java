package com.example.demo.dto;

import com.example.demo.model.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceDTO(
        Long id,
        Long employeeId,
        String employeeName,
        LocalDate workDate,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime,
        AttendanceStatus status,
        Long workedMinutes
) {}
