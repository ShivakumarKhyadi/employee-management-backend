package com.example.demo.dto;

import com.example.demo.model.CalculationType;
import com.example.demo.model.ComponentType;

public record SalaryComponentDTO(
        Long id, String name, ComponentType componentType,
        CalculationType calculationType, Double defaultValue, boolean active
) {}