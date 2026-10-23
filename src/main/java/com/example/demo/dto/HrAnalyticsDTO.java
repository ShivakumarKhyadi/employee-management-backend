package com.example.demo.dto;

import java.util.Map;

public record HrAnalyticsDTO(
        long totalEmployees,
        long newThisMonth,
        long onLeaveToday,
        long pendingLeaveRequests,
        double attritionRatePercent,
        Double avgPerformanceScore,
        Map<String, Long> departmentCounts,
        Map<String, Double> departmentPercent,
        Map<String, Long> performanceDistribution
) {}