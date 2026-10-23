package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "grade")
    private Grade grade;

    @Column(name = "photo_filename")
    private String photoFilename;

    @Email(message = "Enter a valid email address")
    @Column(name = "project_manager_email")
    private String projectManagerEmail;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private AppUser appUser;

    @ManyToOne
    @JoinColumn(name = "reports_to_emp_id")
    private Employee reportsTo;

    @Transient
    private Long reportsToEmpId;

    @Column(name = "designation")
    private String designation;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;
}
