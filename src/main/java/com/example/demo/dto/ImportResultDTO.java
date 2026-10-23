package com.example.demo.dto;

import java.util.List;

public record ImportResultDTO(
    int totalRows,
    int importedCount,
    int skippedCount,
    List<ImportRowError> errors
) {}
