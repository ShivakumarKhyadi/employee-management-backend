package com.example.demo.dto;

import java.time.LocalDateTime;

public record SupportThreadSummaryDTO (
        Long employeeId,
        String employeeName,
        String otherPartyUsername,
        String lastMessage,
        LocalDateTime lastSentDate
){}
