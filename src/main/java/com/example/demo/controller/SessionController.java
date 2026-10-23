package com.example.demo.controller;

import com.example.demo.model.UserSession;
import com.example.demo.repository.UserSessionRepository;
import com.example.demo.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired private SessionService sessionService;
    @Autowired private UserSessionRepository sessionRepository;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> getStats(
            @RequestParam(required = false) Integer days) {
        return ResponseEntity.ok(sessionService.getStats(days));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam(required = false) Integer days) {
        return ResponseEntity.ok(sessionService.getSummary(days));
    }

    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
//    @PreAuthorize("hasAuthority('SEE_GET_DAILY')")
    public ResponseEntity<List<Map<String, Object>>> getDaily(
            @RequestParam(defaultValue = "14") int days) {
        return ResponseEntity.ok(sessionService.getDaily(days));
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
//    @PreAuthorize("hasAuthority('GET_USER_SESSIONS')")
    public ResponseEntity<List<UserSession>> getUserSessions(@PathVariable String username) {
        return ResponseEntity.ok(sessionService.getUserSessions(username));
    }

    @GetMapping("/activity")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> getUserActivity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(sessionService.getUserActivity(fromDate, toDate));
    }

    @GetMapping("/user/{username}/activity")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<List<UserSession>> getUserActivityDetail(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(sessionService.getUserActivityDetail(username, fromDate, toDate));
    }

    @GetMapping("/locations")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> getSessionLocations(
            @RequestParam(required = false) Integer days) {

        List<UserSession> sessions = sessionRepository.findAll().stream()
                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                .filter(s -> days == null || s.getLoginTime().isAfter(LocalDateTime.now().minusDays(days)))
                .sorted(Comparator.comparing(UserSession::getLoginTime).reversed())
                .toList();

        return ResponseEntity.ok(sessions.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("sessionId", s.getSessionId());
            m.put("username", s.getUsername());
            m.put("latitude", s.getLatitude());
            m.put("longitude", s.getLongitude());
            m.put("loginTime", s.getLoginTime());
            m.put("logoutTime", s.getLogoutTime());
            return m;
        }).collect(Collectors.toList()));
    }
}