package com.example.demo.dto;

public record EmployeeSalaryComponentDTO(
        Long componentId, String componentName, Double effectiveValue, boolean isOverridden
) {}