package com.example.demo.repository;

import com.example.demo.model.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    List<PerformanceReview> findByEmployee_EmpIdOrderByCreatedDateDesc(Long empId);
}
