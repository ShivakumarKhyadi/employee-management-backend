package com.example.demo.dto;

import com.example.demo.model.PerformanceRating;

import java.time.LocalDateTime;

public record PerformanceReviewDTO(
        Long id,
        PerformanceRating rating,
        String comments,
        boolean recommendAction,
        String reviewedBy,
        LocalDateTime reviewedDate
) {}
