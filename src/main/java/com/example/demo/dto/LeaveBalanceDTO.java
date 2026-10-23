package com.example.demo.dto;

import java.util.Map;

public record LeaveBalanceDTO(
        Map<String, Integer> allowedPerType,
        Map<String, Integer> usedPerType,
        Map<String, Integer> remainingPerType
) {}