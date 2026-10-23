package com.example.demo.service;

import com.example.demo.dto.AttendanceDTO;
import com.example.demo.mapper.AttendanceMapper;
import com.example.demo.model.Attendance;
import com.example.demo.model.AttendanceStatus;
import com.example.demo.model.Employee;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private static final LocalTime LATE_CUTOFF = LocalTime.of(9, 15);

    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private EmployeeRepository employeeRepository;

    @Transactional
    public AttendanceDTO checkIn(Long employeeId) {
        LocalDate today = LocalDate.now();

        Attendance existing = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today).orElse(null);
        if (existing != null && existing.getCheckInTime() != null) {
            throw new IllegalStateException("You have already checked in today");
        }

        Attendance attendance = existing != null ? existing : new Attendance();
        LocalDateTime now = LocalDateTime.now();

        attendance.setEmployeeId(employeeId);
        attendance.setWorkDate(today);
        attendance.setCheckInTime(now);
        attendance.setStatus(now.toLocalTime().isAfter(LATE_CUTOFF) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT);

        attendance = attendanceRepository.save(attendance);
        return AttendanceMapper.toDto(attendance, nameOf(employeeId));
    }

    @Transactional
    public AttendanceDTO checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new IllegalStateException("You have not checked in today"));

        if (attendance.getCheckInTime() == null) {
            throw new IllegalStateException("You have not checked in today");
        }
        if (attendance.getCheckOutTime() != null) {
            throw new IllegalStateException("You have already checked out today");
        }

        LocalDateTime now = LocalDateTime.now();
        attendance.setCheckOutTime(now);
        attendance.setWorkedMinutes(Duration.between(attendance.getCheckInTime(), now).toMinutes());

        attendance = attendanceRepository.save(attendance);
        return AttendanceMapper.toDto(attendance, nameOf(employeeId));
    }

    public AttendanceDTO getToday(Long employeeId) {
        LocalDate today = LocalDate.now();
        return attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .map(a -> AttendanceMapper.toDto(a, nameOf(employeeId)))
                .orElse(new AttendanceDTO(null, employeeId, nameOf(employeeId), today, null, null, null, null));
    }

    public List<AttendanceDTO> getHistory(Long employeeId, LocalDate from, LocalDate to) {
        String name = nameOf(employeeId);
        return attendanceRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(employeeId, from, to)
                .stream()
                .map(a -> AttendanceMapper.toDto(a, name))
                .collect(Collectors.toList());
    }

    public List<AttendanceDTO> getForDate(LocalDate date) {
        return attendanceRepository.findByWorkDateOrderByEmployeeIdAsc(date).stream()
                .map(a -> AttendanceMapper.toDto(a, nameOf(a.getEmployeeId())))
                .collect(Collectors.toList());
    }

    private String nameOf(Long employeeId) {
        return employeeRepository.findById(employeeId).map(Employee::getName).orElse("Unknown");
    }
}
