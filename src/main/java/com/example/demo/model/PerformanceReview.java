package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "performance_reviews")
@EntityListeners(AuditingEntityListener.class)
@Data
public class PerformanceReview extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "emp_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerformanceRating rating;

    @Column(length = 1000)
    private String comments;

    @Column(name = "recommend_action", nullable = false)
    private boolean recommendAction = false;
}
