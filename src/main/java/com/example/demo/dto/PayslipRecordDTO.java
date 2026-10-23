package com.example.demo.dto;

import java.time.LocalDateTime;

public record PayslipRecordDTO(
        Long id,
        String payMonth,
        Double basicPay,
        Double hra,
        Double otherAllowance,
        Double grossPay,
        Double providentFund,
        Double professionalTax,
        Double netPay,
        String generatedBy,
        LocalDateTime generatedDate
) {}