package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_runs", uniqueConstraints = @UniqueConstraint(columnNames = "pay_month"))
@Data
public class PayrollRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pay_month", nullable = false, length = 7)
    private String payMonth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status = PayrollStatus.DRAFT;

    @Column(name = "run_by")
    private String runBy;

    @Column(name = "run_date")
    private LocalDateTime runDate;

    @Column(name = "employee_count")
    private Integer employeeCount;
}