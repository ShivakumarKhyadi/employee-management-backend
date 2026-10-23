package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "payslip_records", uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "pay_month"}))
@Data
public class PayslipRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "pay_month", nullable = false, length = 7)
    private String payMonth;

    @Column(name = "gross_pay", nullable = false)
    private Double grossPay;

    @Column(name = "net_pay", nullable = false)
    private Double netPay;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "generated_by")
    private String generatedBy;

    @Column(name = "generated_date", nullable = false)
    private LocalDateTime generatedDate;

    @Column(name = "basic_pay", nullable = false)
    private Double basicPay;

    @Column(name = "hra", nullable = false)
    private Double hra;

    @Column(name = "other_allowance", nullable = false)
    private Double otherAllowance;

    @Column(name = "provident_fund", nullable = false)
    private Double providentFund;

    @Column(name = "professional_tax", nullable = false)
    private Double professionalTax;
}
