package com.example.demo.mapper;

import com.example.demo.dto.DepartmentDTO;
import com.example.demo.model.Department;

public class DepartmentMapper {

    public static DepartmentDTO toDto(Department d) {
        return new DepartmentDTO(
                d.getDeptId(),
                d.getDepartmentCode(),
                d.getDepartmentName(),
                d.getStatus(),
                d.getCreatedBy(),
                d.getCreatedDate(),
                d.getLastModifiedBy(),
                d.getLastModifiedDate()
        );
    }

    public static void applyToEntity(DepartmentDTO dto, Department entity) {
        entity.setDepartmentName(dto.departmentName());
        entity.setStatus(dto.status());
    }
}