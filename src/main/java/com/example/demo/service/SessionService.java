package com.example.demo.service;

import com.example.demo.model.UserSession;
import com.example.demo.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SessionService {

    @Autowired private UserSessionRepository sessionRepository;

    public List<Map<String, Object>> getStats(Integer days) {
        List<UserSession> all = filterByDays(days);

        Map<String, Long> totalsByUser = all.stream()
                .filter(s -> s.getDurationSeconds() != null)
                .collect(Collectors.groupingBy(
                        UserSession::getUsername,
                        Collectors.summingLong(UserSession::getDurationSeconds)));

        return totalsByUser.entrySet().stream()
                .map(e -> Map.<String, Object>of(
                        "username", e.getKey(),
                        "totalSeconds", e.getValue()))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getSummary(Integer days) {
        List<UserSession> all = filterByDays(days).stream()
                .filter(s -> s.getDurationSeconds() != null)
                .toList();

        long totalSeconds = all.stream().mapToLong(UserSession::getDurationSeconds).sum();
        long sessionCount = all.size();
        long avgSeconds = sessionCount > 0 ? totalSeconds / sessionCount : 0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSeconds", totalSeconds);
        summary.put("sessionCount", sessionCount);
        summary.put("avgSeconds", avgSeconds);
        return summary;
    }

    public List<Map<String, Object>> getDaily(int days) {
        List<UserSession> all = sessionRepository.findAll().stream()
                .filter(s -> s.getLoginTime().isAfter(LocalDateTime.now().minusDays(days)))
                .toList();

        Map<String, Long> byDay = all.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getLoginTime().toLocalDate().toString(),
                        Collectors.counting()));

        return byDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> Map.<String, Object>of("date", e.getKey(), "count", e.getValue()))
                .collect(Collectors.toList());
    }

    public List<UserSession> getUserSessions(String username) {
        return sessionRepository.findAll().stream()
                .filter(s -> s.getUsername().equalsIgnoreCase(username))
                .sorted(Comparator.comparing(UserSession::getLoginTime).reversed())
                .toList();
    }

    public List<Map<String, Object>> getUserActivity(LocalDate fromDate, LocalDate toDate) {
        List<UserSession> sessions = filterByDateRange(fromDate, toDate).stream()
                .filter(s -> s.getDurationSeconds() != null)
                .toList();

        Map<String, Long> totalsByUser = sessions.stream()
                .collect(Collectors.groupingBy(UserSession::getUsername,
                        Collectors.summingLong(UserSession::getDurationSeconds)));

        Map<String, Long> countByUser = sessions.stream()
                .collect(Collectors.groupingBy(UserSession::getUsername, Collectors.counting()));

        return totalsByUser.entrySet().stream()
                .map(e -> Map.<String, Object>of(
                        "username", e.getKey(),
                        "totalSeconds", e.getValue(),
                        "sessionCount", countByUser.getOrDefault(e.getKey(), 0L)))
                .collect(Collectors.toList());
    }

    public List<UserSession> getUserActivityDetail(String username, LocalDate fromDate, LocalDate toDate) {
        return filterByDateRange(fromDate, toDate).stream()
                .filter(s -> s.getUsername().equalsIgnoreCase(username))
                .sorted(Comparator.comparing(UserSession::getLoginTime).reversed())
                .toList();
    }


    private List<UserSession> filterByDays(Integer days) {
        return sessionRepository.findAll().stream()
                .filter(s -> days == null || s.getLoginTime().isAfter(LocalDateTime.now().minusDays(days)))
                .toList();
    }

    private List<UserSession> filterByDateRange(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : LocalDateTime.MIN;
        LocalDateTime to = toDate != null ? toDate.atTime(23, 59, 59) : LocalDateTime.MAX;

        return sessionRepository.findAll().stream()
                .filter(s -> !s.getLoginTime().isBefore(from) && !s.getLoginTime().isAfter(to))
                .toList();
    }
}