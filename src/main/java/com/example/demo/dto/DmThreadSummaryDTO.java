package com.example.demo.dto;

import java.time.LocalDateTime;

public record DmThreadSummaryDTO(
        String username,
        String displayName,
        String roleCode,
        String lastMessage,
        LocalDateTime lastSentDate,
        long unreadCount
) {}