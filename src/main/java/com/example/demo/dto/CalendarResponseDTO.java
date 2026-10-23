package com.example.demo.dto;

import java.util.List;

public record CalendarResponseDTO(
        List<HolidayDTO> holidays,
        List<CalendarLeaveDTO> leaves
) {}