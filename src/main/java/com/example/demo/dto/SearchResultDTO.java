package com.example.demo.dto;

public record SearchResultDTO(
        String type,
        Long id,
        String label,
        String subtitle,
        String route
) {}