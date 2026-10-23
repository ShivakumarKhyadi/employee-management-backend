package com.example.demo.controller;

import com.example.demo.dto.PerformanceReviewDTO;
import com.example.demo.dto.PerformanceSummaryDTO;
import com.example.demo.model.PerformanceRating;
import com.example.demo.service.PerformanceReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees/{empId}/performance")
public class PerformanceReviewController {

    @Autowired private PerformanceReviewService performanceReviewService;

    public record ReviewRequest(PerformanceRating rating, String comments, boolean recommendAction) {}

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<PerformanceReviewDTO> addReview(@PathVariable Long empId, @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(performanceReviewService.addReview(
                empId, request.rating(), request.comments(), request.recommendAction()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or #empId == authentication.principal.employeeId")
    public ResponseEntity<PerformanceSummaryDTO> getSummary(@PathVariable Long empId) {
        return ResponseEntity.ok(performanceReviewService.getSummary(empId));
    }
}