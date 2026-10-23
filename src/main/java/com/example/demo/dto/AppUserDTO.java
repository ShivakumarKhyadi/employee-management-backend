package com.example.demo.dto;

import java.time.LocalDateTime;

public record AppUserDTO(
        Integer userId,
        String username,
        String roleCode,
        String roleName,
        String mobileNumber,
        String createdBy,
        LocalDateTime createdDate,
        String lastModifiedBy,
        LocalDateTime lastModifiedDate
) {}