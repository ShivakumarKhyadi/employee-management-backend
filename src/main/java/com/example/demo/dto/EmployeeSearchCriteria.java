package com.example.demo.dto;

import com.example.demo.model.Grade;
import lombok.Data;

@Data
public class EmployeeSearchCriteria {
    private String search;
    private Long departmentId;
    private Double minSalary;
    private Double maxSalary;
    private Grade grade;
    private Boolean supervisorOnly;
}
