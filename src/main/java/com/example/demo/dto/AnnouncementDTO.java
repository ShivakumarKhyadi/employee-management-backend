package com.example.demo.dto;

import com.example.demo.model.AnnouncementCategory;

import java.time.LocalDateTime;

public record AnnouncementDTO(
        Long id,
        String title,
        String message,
        AnnouncementCategory category,
        boolean pinned,
        String createdBy,
        LocalDateTime createdDate,
        String lastModifiedBy,
        LocalDateTime lastModifiedDate
) {}