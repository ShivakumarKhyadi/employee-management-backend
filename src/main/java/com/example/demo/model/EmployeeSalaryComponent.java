package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "employee_salary_components", uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "component_id"}))
@Data
public class EmployeeSalaryComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "component_id", nullable = false)
    private Long componentId;

    @Column(name = "override_value")
    private Double overrideValue;
}