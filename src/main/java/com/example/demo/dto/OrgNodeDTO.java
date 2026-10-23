package com.example.demo.dto;

import java.util.List;

public record OrgNodeDTO(
        Long empId,
        String name,
        String departmentName,
        String grade,
        String designation,
        List<OrgNodeDTO> children
) {}