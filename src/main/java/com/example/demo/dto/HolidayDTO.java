package com.example.demo.dto;

import java.time.LocalDate;

public record HolidayDTO(
        Long id,
        String name,
        LocalDate date,
        String description,
        boolean optional
) {}