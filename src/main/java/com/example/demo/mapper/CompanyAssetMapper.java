package com.example.demo.mapper;

import com.example.demo.dto.CompanyAssetDTO;
import com.example.demo.model.CompanyAsset;

public class CompanyAssetMapper {

    public static CompanyAssetDTO toDto(CompanyAsset a){
        return new CompanyAssetDTO(
                a.getId(),
                a.getAssetName(),
                a.getAssetType(),
                a.getSerialNumber(),
                a.getEmployee() != null ? a.getEmployee().getEmpId() : null,
                a.getEmployee() != null ? a.getEmployee().getName() : null,
                a.getStatus(),
                a.getAssignedDate(),
                a.getReturnDueDate(),
                a.getReturnedDate(),
                a.getNotes(),
                a.getCreatedBy(),
                a.getCreatedDate(),
                a.getLastModifiedBy(),
                a.getLastModifiedDate()
        );
    }
}
