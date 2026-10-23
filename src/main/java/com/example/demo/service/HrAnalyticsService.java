package com.example.demo.service;

import com.example.demo.dto.EmployeeNameDTO;
import com.example.demo.dto.HrAnalyticsDTO;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HrAnalyticsService {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private EmployeeExitLogRepository employeeExitLogRepository;
    @Autowired private LeaveRequestRepository leaveRequestRepository;
    @Autowired private PerformanceReviewRepository performanceReviewRepository;
    @Autowired private FileRepository fileRepository;

    public HrAnalyticsDTO getAnalytics() {
        List<Employee> all = employeeRepository.findAll();
        long total = all.size();

        YearMonth thisMonth = YearMonth.now();
        long newThisMonth = all.stream()
                .filter(e -> e.getJoiningDate() != null && YearMonth.from(e.getJoiningDate()).equals(thisMonth))
                .count();

        LocalDate today = LocalDate.now();
        long onLeaveToday = leaveRequestRepository.findByStatusOrderByAppliedDateAsc(LeaveStatus.APPROVED).stream()
                .filter(r -> !today.isBefore(r.getStartDate()) && !today.isAfter(r.getEndDate()))
                .map(LeaveRequest::getEmployeeId)
                .distinct()
                .count();

        long pendingLeaveRequests = leaveRequestRepository.findByStatusOrderByAppliedDateAsc(LeaveStatus.PENDING).size();

        LocalDate oneYearAgo = today.minusYears(1);
        long exitsLastYear = employeeExitLogRepository.findByExitDateAfter(oneYearAgo).size();
        double avgHeadcount = (total + exitsLastYear) / 2.0;
        double attritionRate = avgHeadcount > 0 ? (exitsLastYear / avgHeadcount) * 100.0 : 0.0;

        List<PerformanceReview> reviews = performanceReviewRepository.findAll();
        Double avgPerformance = reviews.isEmpty() ? null :
                reviews.stream().mapToInt(r -> scoreOf(r.getRating())).average().orElse(0.0);

        Map<String, Long> deptCounts = all.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getDepartment() != null ? e.getDepartment().getDepartmentName() : "Unassigned",
                        Collectors.counting()));

        Map<String, Double> deptPercent = new LinkedHashMap<>();
        deptCounts.forEach((k, v) -> deptPercent.put(k, total > 0 ? Math.round((v * 1000.0 / total)) / 10.0 : 0.0));

        Map<String, Long> perfDistribution = new LinkedHashMap<>();
        for (PerformanceRating r : PerformanceRating.values()) perfDistribution.put(r.name(), 0L);
        reviews.forEach(r -> perfDistribution.merge(r.getRating().name(), 1L, Long::sum));

        return new HrAnalyticsDTO(
                total, newThisMonth, onLeaveToday,pendingLeaveRequests,
                Math.round(attritionRate * 10) / 10.0,
                avgPerformance != null ? Math.round(avgPerformance * 20 * 10) / 10.0 : null,
                deptCounts, deptPercent, perfDistribution
        );
    }

    public Map<String, Long> getDocumentExpirySummary() {
        List<FileEntity> files = fileRepository.findAll();
        LocalDate today = LocalDate.now();
        long expiring = files.stream().filter(f -> f.getExpiryDate() != null && !f.getExpiryDate().isBefore(today)
                && ChronoUnit.DAYS.between(today, f.getExpiryDate()) <= 15).count();
        long expired = files.stream().filter(f -> f.getExpiryDate() != null && f.getExpiryDate().isBefore(today)).count();
        return Map.of("expiringSoon", expiring, "expired", expired);
    }

    private int scoreOf(PerformanceRating rating) {
        return switch (rating) {
            case POOR -> 1;
            case NEEDS_IMPROVEMENT -> 2;
            case SATISFACTORY -> 3;
            case GOOD -> 4;
            case EXCELLENT -> 5;
        };
    }

    public List<EmployeeNameDTO> getEmployeesInDepartment(String deptName) {
        return employeeRepository.findAll().stream()
                .filter(e -> deptName.equals(e.getDepartment() != null ? e.getDepartment().getDepartmentName() : "Unassigned"))
                .map(e -> new EmployeeNameDTO(e.getEmpId(), e.getName()))
                .collect(Collectors.toList());
    }

    public List<EmployeeNameDTO> getEmployeesWithRating(String ratingName) {
        PerformanceRating rating;
        try {
            rating = PerformanceRating.valueOf(ratingName);
        } catch (IllegalArgumentException ex) {
            return List.of();
        }
        return performanceReviewRepository.findAll().stream()
                .filter(r -> r.getRating() == rating)
                .map(r -> new EmployeeNameDTO(r.getEmployee().getEmpId(), r.getEmployee().getName()))
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getOnLeaveTodayList() {
        LocalDate today = LocalDate.now();
        return leaveRequestRepository.findByStatusOrderByAppliedDateAsc(LeaveStatus.APPROVED).stream()
                .filter(r -> !today.isBefore(r.getStartDate()) && !today.isAfter(r.getEndDate()))
                .map(r -> {
                    Employee e = employeeRepository.findById(r.getEmployeeId()).orElse(null);
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("empId", r.getEmployeeId());
                    row.put("name", e != null ? e.getName() : "Unknown");
                    row.put("leaveType", r.getLeaveType().name());
                    row.put("returnsOn", r.getEndDate());
                    return row;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getNewThisMonthList() {
        YearMonth thisMonth = YearMonth.now();
        return employeeRepository.findAll().stream()
                .filter(e -> e.getJoiningDate() != null && YearMonth.from(e.getJoiningDate()).equals(thisMonth))
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("empId", e.getEmpId());
                    row.put("name", e.getName());
                    row.put("departmentName", e.getDepartment() != null ? e.getDepartment().getDepartmentName() : "Unassigned");
                    row.put("joiningDate", e.getJoiningDate());
                    return row;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getEmployeesByLatestRating(PerformanceRating rating) {
        Map<Long, PerformanceReview> latestByEmployee = new LinkedHashMap<>();
        performanceReviewRepository.findAll().forEach(r -> {
            Long empId = r.getEmployee().getEmpId();
            PerformanceReview current = latestByEmployee.get(empId);
            if (current == null || r.getCreatedDate().isAfter(current.getCreatedDate())) {
                latestByEmployee.put(empId, r);
            }
        });

        return latestByEmployee.values().stream()
                .filter(r -> r.getRating() == rating)
                .map(r -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("empId", r.getEmployee().getEmpId());
                    row.put("name", r.getEmployee().getName());
                    row.put("ratedDate", r.getCreatedDate());
                    return row;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getEmployeesByDepartment(String departmentName) {
        return employeeRepository.findAll().stream()
                .filter(e -> e.getDepartment() != null && e.getDepartment().getDepartmentName().equalsIgnoreCase(departmentName))
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("empId", e.getEmpId());
                    row.put("name", e.getName());
                    row.put("email", e.getEmail());
                    row.put("grade", e.getGrade() != null ? e.getGrade().name() : null);
                    return row;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getSupervisorsList() {
        return employeeRepository.findAll().stream()
                .filter(Employee::isSupervisor)
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("empId", e.getEmpId());
                    row.put("name", e.getName());
                    row.put("departmentName", e.getDepartment() != null ? e.getDepartment().getDepartmentName() : "Unassigned");
                    return row;
                })
                .collect(Collectors.toList());
    }
}