package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "department")
@EqualsAndHashCode(callSuper = false)
@Data
public class Department extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_id")
    private Long deptId;

    @Version
    private Integer version;

    @NotBlank(message = "Department code is required")
    @Column(name = "department_code", unique = true, nullable = false)
    private String departmentCode;

    @NotBlank(message = "Department name is required")
    @Column(name = "department_name", nullable = false)
    private String departmentName;

//    @Column(name = "created_date")
//    private LocalDate createdDate;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepartmentStatus status;

}
