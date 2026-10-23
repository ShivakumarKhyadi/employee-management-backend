package com.example.demo.dto;

public record TypingEvent(
        String fromUsername,
        String toUsername,
        boolean typing
) {}
