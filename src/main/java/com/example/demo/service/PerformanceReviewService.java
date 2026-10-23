package com.example.demo.service;

import com.example.demo.dto.PerformanceReviewDTO;
import com.example.demo.dto.PerformanceSummaryDTO;
import com.example.demo.mapper.PerformanceReviewMapper;
import com.example.demo.model.Employee;
import com.example.demo.model.PerformanceRating;
import com.example.demo.model.PerformanceReview;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.PerformanceReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerformanceReviewService {

    @Autowired private PerformanceReviewRepository performanceReviewRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private EmailService emailService;

    public PerformanceReviewDTO addReview(Long empId, PerformanceRating rating, String comments, boolean recommendAction) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        PerformanceReview review = new PerformanceReview();
        review.setEmployee(employee);
        review.setRating(rating);
        review.setComments(comments);
        review.setRecommendAction(recommendAction);
        review = performanceReviewRepository.save(review);

        if (employee.getEmail() != null) {
            emailService.send(
                    employee.getEmail(),
                    "Performance review recorded",
                    "Hello " + employee.getName() + ",\n\nA performance review has been recorded for you with rating: "
                            + rating.name() + ".\n\nComments: " + comments
            );
        }

        if (recommendAction) {
            notificationService.notifyAdminsAndSupervisors(
                    "PERFORMANCE_FLAG",
                    employee.getName() + " was flagged for review after a " + rating.name() + " rating",
                    "/employees/" + empId
            );
        }

        return PerformanceReviewMapper.toDto(review);
    }

    public PerformanceSummaryDTO getSummary(Long empId) {
        List<PerformanceReview> reviews = performanceReviewRepository
                .findByEmployee_EmpIdOrderByCreatedDateDesc(empId);

        List<PerformanceReviewDTO> dtos = reviews.stream()
                .map(PerformanceReviewMapper::toDto)
                .collect(Collectors.toList());

        double average = reviews.isEmpty() ? 0.0 :
                reviews.stream().mapToInt(r -> scoreOf(r.getRating())).average().orElse(0.0);

        PerformanceRating latest = reviews.isEmpty() ? null : reviews.get(0).getRating();
        boolean hasOpenFlag = !reviews.isEmpty() && reviews.get(0).isRecommendAction();

        return new PerformanceSummaryDTO(
                Math.round(average * 10) / 10.0,
                latest,
                reviews.size(),
                hasOpenFlag,
                dtos
        );
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
}