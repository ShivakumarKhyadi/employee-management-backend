package com.example.demo.dto;

import java.time.LocalDateTime;

public record ReadReceiptEvent(
        String readByUsername,
        String conversationWith,
        LocalDateTime readAt
) {}
