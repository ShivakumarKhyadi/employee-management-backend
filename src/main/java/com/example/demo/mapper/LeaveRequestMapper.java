package com.example.demo.mapper;

import com.example.demo.dto.LeaveRequestDTO;
import com.example.demo.model.LeaveRequest;
import java.time.temporal.ChronoUnit;

public class LeaveRequestMapper {
    public static LeaveRequestDTO toDto(LeaveRequest r, String employeeName) {
        long days = ChronoUnit.DAYS.between(r.getStartDate(), r.getEndDate()) + 1;
        return new LeaveRequestDTO(
                r.getId(), r.getEmployeeId(), employeeName, r.getLeaveType(),
                r.getStartDate(), r.getEndDate(), days, r.getReason(), r.getStatus(),
                r.getAppliedDate(), r.getDecidedBy(), r.getDecidedDate(), r.getDecisionNote()
        );
    }
}