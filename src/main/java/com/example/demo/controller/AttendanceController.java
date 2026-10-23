package com.example.demo.controller;

import com.example.demo.dto.AttendanceDTO;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired private AttendanceService attendanceService;

    private Long employeeIdOf(Authentication auth) {
        return (auth.getPrincipal() instanceof CustomUserDetails cud) ? cud.getEmployeeId() : null;
    }

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(Authentication auth) {
        Long employeeId = employeeIdOf(auth);
        if (employeeId == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(attendanceService.checkIn(employeeId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/check-out")
    public ResponseEntity<?> checkOut(Authentication auth) {
        Long employeeId = employeeIdOf(auth);
        if (employeeId == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(attendanceService.checkOut(employeeId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/today")
    public ResponseEntity<AttendanceDTO> today(Authentication auth) {
        Long employeeId = employeeIdOf(auth);
        if (employeeId == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(attendanceService.getToday(employeeId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AttendanceDTO>> history(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication auth) {
        Long employeeId = employeeIdOf(auth);
        if (employeeId == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(attendanceService.getHistory(employeeId, from, to));
    }

    @GetMapping("/employee/{employeeId}/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<AttendanceDTO>> historyForEmployee(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(attendanceService.getHistory(employeeId, from, to));
    }

    @GetMapping("/team")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<AttendanceDTO>> team(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getForDate(date != null ? date : LocalDate.now()));
    }
}
