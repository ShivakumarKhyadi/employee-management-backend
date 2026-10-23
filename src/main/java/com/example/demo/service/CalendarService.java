package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.mapper.HolidayMapper;
import com.example.demo.model.Employee;
import com.example.demo.model.LeaveRequest;
import com.example.demo.model.LeaveStatus;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.HolidayRepository;
import com.example.demo.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    @Autowired private HolidayRepository holidayRepository;
    @Autowired private LeaveRequestRepository leaveRequestRepository;
    @Autowired private EmployeeRepository employeeRepository;

    public CalendarResponseDTO getCalendar(LocalDate start, LocalDate end, boolean includeTeamLeave) {
        List<HolidayDTO> holidays = holidayRepository.findByDateBetweenOrderByDateAsc(start, end).stream()
                .map(HolidayMapper::toDto)
                .collect(Collectors.toList());

        if (!includeTeamLeave) {
            return new CalendarResponseDTO(holidays, List.of());
        }

        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findApprovedOverlapping(LeaveStatus.APPROVED, start, end);
        Map<Long, String> nameCache = new HashMap<>();

        List<CalendarLeaveDTO> leaves = approvedLeaves.stream()
                .map(l -> new CalendarLeaveDTO(
                        l.getEmployeeId(),
                        nameCache.computeIfAbsent(l.getEmployeeId(), id ->
                                employeeRepository.findById(id).map(Employee::getName).orElse("Unknown")),
                        l.getLeaveType(),
                        l.getStartDate(),
                        l.getEndDate()
                ))
                .collect(Collectors.toList());

        return new CalendarResponseDTO(holidays, leaves);
    }
}