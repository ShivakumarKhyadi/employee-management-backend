package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Table(name="employee")
@EqualsAndHashCode(callSuper = false)
@Data
public class Employee extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_id")
    private Long empId;
    private String name;
    private String email;
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Transient
    private Long departmentId;
    private Double salary;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "is_supervisor")
    private boolean supervisor;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type")
    private EmployeeType employeeType;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "grade")
    private Grade grade;


}
