package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "payroll_entries")
@Data
public class PayrollEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false)
    private Long runId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "basic_pay", nullable = false)
    private Double basicPay;

    @Column(name = "gross_pay", nullable = false)
    private Double grossPay;

    @Column(name = "total_deductions", nullable = false)
    private Double totalDeductions;

    @Column(name = "net_pay", nullable = false)
    private Double netPay;

    @Column(name = "breakdown_json", length = 2000)
    private String breakdownJson;
}