package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "employee_exit_log")
@Data
public class EmployeeExitLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String employeeName;
    private String departmentName;

    @Column(name = "exit_date", nullable = false)
    private LocalDate exitDate;
}