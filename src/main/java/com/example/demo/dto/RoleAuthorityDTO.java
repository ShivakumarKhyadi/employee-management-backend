package com.example.demo.dto;

import java.time.LocalDateTime;

public record RoleAuthorityDTO(
        Long id,
        String roleCode,
        String authorityName,
        String createdBy,
        LocalDateTime createdDate,
        String lastModifiedBy,
        LocalDateTime lastModifiedDate
) {}