package com.example.demo.dto;

import com.example.demo.model.PerformanceRating;

import java.util.List;

public record PerformanceSummaryDTO(
        double averageScore,
        PerformanceRating latestRating,
        int totalReviews,
        boolean hasOpenFlag,
        List<PerformanceReviewDTO> reviews
) {}
