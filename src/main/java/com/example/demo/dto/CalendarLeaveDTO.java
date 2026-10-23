package com.example.demo.dto;

import com.example.demo.model.LeaveType;
import java.time.LocalDate;

public record CalendarLeaveDTO(
        Long employeeId,
        String employeeName,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate
) {}