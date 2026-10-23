package com.example.demo.mapper;

import com.example.demo.dto.AttendanceDTO;
import com.example.demo.model.Attendance;

public class AttendanceMapper {
    public static AttendanceDTO toDto(Attendance a, String employeeName) {
        return new AttendanceDTO(
                a.getId(),
                a.getEmployeeId(),
                employeeName,
                a.getWorkDate(),
                a.getCheckInTime(),
                a.getCheckOutTime(),
                a.getStatus(),
                a.getWorkedMinutes()
        );
    }
}
