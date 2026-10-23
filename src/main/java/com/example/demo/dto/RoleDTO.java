package com.example.demo.dto;

import java.time.LocalDateTime;

public record RoleDTO(
        Long id,
        String roleCode,
        String roleName,
        String createdBy,
        LocalDateTime createdDate,
        String lastModifiedBy,
        LocalDateTime lastModifiedDate
) {}