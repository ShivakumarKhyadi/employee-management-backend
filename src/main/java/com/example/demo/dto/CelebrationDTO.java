package com.example.demo.dto;

import java.time.LocalDate;

public record CelebrationDTO(
        Long empId,
        String name,
        String type,
        LocalDate date,
        int years,
        boolean isToday
) {}