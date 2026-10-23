package com.example.demo.dto;

import com.example.demo.model.DepartmentStatus;
import lombok.Data;

@Data
public class DepartmentSearchCriteria {

    private String search;
    private DepartmentStatus status;
}
