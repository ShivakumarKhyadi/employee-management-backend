package com.example.demo.mapper;

import com.example.demo.dto.AppUserDTO;
import com.example.demo.model.AppUser;

public class UserMapper {

    public static AppUserDTO toDto(AppUser u) {
        return new AppUserDTO(
                u.getUserId(),
                u.getUsername(),
                u.getRole() != null ? u.getRole().getRoleCode() : null,
                u.getRole() != null ? u.getRole().getRoleName() : null,
                u.getMobileNumber(),
                u.getCreatedBy(),
                u.getCreatedDate(),
                u.getLastModifiedBy(),
                u.getLastModifiedDate()
        );
    }
}