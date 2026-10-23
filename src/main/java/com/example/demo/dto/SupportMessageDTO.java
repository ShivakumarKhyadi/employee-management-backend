package com.example.demo.dto;

import com.example.demo.model.SenderRole;

import java.time.LocalDateTime;

public record SupportMessageDTO(
        Long id,
        String senderUsername,
        String receiverUsername,
        SenderRole senderRole,
        String message,
        LocalDateTime sentDate,
        boolean isRead,
        LocalDateTime readAt,
        String attachmentUrl,
        String attachmentName,
        String attachmentType,
        Long attachmentSize
) {}