package com.example.demo.dto;

import com.example.demo.model.AssetStatus;
import com.example.demo.model.AssetType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CompanyAssetDTO(
        Long id,
        String assetsName,
        AssetType assetType,
        String serialNumber,
        Long employeeId,
        String employeeName,
        AssetStatus status,
        LocalDate assignedDate,
        LocalDate returnDueDate,
        LocalDate returnedDate,
        String notes,
        String createdBy,
        LocalDateTime createdDate,
        String lastModifiedBy,
        LocalDateTime lastModifiedDate
) {}
