package com.example.demo.mapper;

import com.example.demo.dto.ChecklistItemDTO;
import com.example.demo.model.ChecklistItem;

public class ChecklistItemMapper {
    public static ChecklistItemDTO toDto(ChecklistItem c) {
        return new ChecklistItemDTO(
                c.getId(), c.getEmployeeId(), c.getEmployeeName(),
                c.getCategory(), c.getTaskName(), c.isCompleted(), c.getCompletedDate()
        );
    }
}