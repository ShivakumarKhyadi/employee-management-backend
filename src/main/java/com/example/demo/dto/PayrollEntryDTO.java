package com.example.demo.dto;

import java.util.List;

public record PayrollEntryDTO(
        Long id, Long employeeId, String employeeName, String payMonth,
        Double basicPay, Double grossPay, Double totalDeductions, Double netPay,
        List<ComponentLine> breakdown
) {
    public record ComponentLine(String name, String type, Double amount) {}
}