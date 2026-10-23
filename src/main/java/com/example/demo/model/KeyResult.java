package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "key_results")
@Getter
@Setter
public class KeyResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "objective_id", nullable = false)
    private Objective objective;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double targetValue;

    @Column(nullable = false)
    private Double currentValue = 0.0;

    private String unit;
}