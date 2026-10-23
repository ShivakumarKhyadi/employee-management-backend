package com.example.demo.dto;

import java.time.LocalDateTime;

public record NotificationDTO(
        Long id,
        String type,
        String message,
        String link,
        boolean isRead,
        LocalDateTime createdDate
) {}