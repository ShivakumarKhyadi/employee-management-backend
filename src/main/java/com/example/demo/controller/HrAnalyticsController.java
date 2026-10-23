package com.example.demo.controller;

import com.example.demo.dto.EmployeeNameDTO;
import com.example.demo.dto.HrAnalyticsDTO;
import com.example.demo.service.HrAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class HrAnalyticsController {

    @Autowired private HrAnalyticsService hrAnalyticsService;

    @GetMapping("/hr")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<HrAnalyticsDTO> getHrAnalytics() {
        return ResponseEntity.ok(hrAnalyticsService.getAnalytics());
    }

    @GetMapping("/documents")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Long>> getDocumentSummary() {
        return ResponseEntity.ok(hrAnalyticsService.getDocumentExpirySummary());
    }

    @GetMapping("/hr/department/{deptName}/employees")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<EmployeeNameDTO>> getEmployeesInDepartment(@PathVariable String deptName) {
        return ResponseEntity.ok(hrAnalyticsService.getEmployeesInDepartment(deptName));
    }

    @GetMapping("/hr/performance/{rating}/employees")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<EmployeeNameDTO>> getEmployeesWithRating(@PathVariable String rating) {
        return ResponseEntity.ok(hrAnalyticsService.getEmployeesWithRating(rating));
    }

    @GetMapping("/on-leave-today")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> getOnLeaveToday() {
        return ResponseEntity.ok(hrAnalyticsService.getOnLeaveTodayList());
    }

    @GetMapping("/new-this-month")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> getNewThisMonth() {
        return ResponseEntity.ok(hrAnalyticsService.getNewThisMonthList());
    }

    @GetMapping("/performance/{rating}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> getByRating(@PathVariable com.example.demo.model.PerformanceRating rating) {
        return ResponseEntity.ok(hrAnalyticsService.getEmployeesByLatestRating(rating));
    }

    @GetMapping("/department/{deptName}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> getByDepartment(@PathVariable String deptName) {
        return ResponseEntity.ok(hrAnalyticsService.getEmployeesByDepartment(deptName));
    }

    @GetMapping("/supervisors")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> getSupervisors() {
        return ResponseEntity.ok(hrAnalyticsService.getSupervisorsList());
    }
}