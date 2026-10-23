package com.example.demo.dto;

import com.example.demo.model.PayrollStatus;
import java.time.LocalDateTime;

public record PayrollRunDTO(
        Long id, String payMonth, PayrollStatus status,
        String runBy, LocalDateTime runDate, Integer employeeCount
) {}