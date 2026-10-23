package com.example.demo.mapper;

import com.example.demo.dto.HolidayDTO;
import com.example.demo.model.Holiday;

public class HolidayMapper {
    public static HolidayDTO toDto(Holiday h) {
        return new HolidayDTO(h.getId(), h.getName(), h.getDate(), h.getDescription(), h.isOptional());
    }
}