package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OneOnOneNoteDTO(
        Long id,
        Long employeeId,
        String employeeName,
        String managerUsername,
        LocalDate meetingDate,
        String discussionNotes,
        String actionItems,
        String createdBy,
        LocalDateTime createdDate
) {}