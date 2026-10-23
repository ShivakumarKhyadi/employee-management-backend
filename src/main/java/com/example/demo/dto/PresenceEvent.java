package com.example.demo.dto;

import java.time.LocalDateTime;

public record PresenceEvent(
        String username,
        boolean online,
        LocalDateTime lastSeen
) {}
