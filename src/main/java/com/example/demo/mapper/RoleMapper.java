package com.example.demo.mapper;

import com.example.demo.dto.AuthorityDTO;
import com.example.demo.dto.RoleAuthorityDTO;
import com.example.demo.dto.RoleDTO;
import com.example.demo.model.Authority;
import com.example.demo.model.Role;
import com.example.demo.model.RoleAuthority;

public class RoleMapper {

    public static RoleDTO toDto(Role r) {
        return new RoleDTO(
                r.getId(),
                r.getRoleCode(),
                r.getRoleName(),
                r.getCreatedBy(),
                r.getCreatedDate(),
                r.getLastModifiedBy(),
                r.getLastModifiedDate()
        );
    }

    public static AuthorityDTO toDto(Authority a) {

        return new AuthorityDTO(a.getName());
    }

    public static RoleAuthorityDTO toDto(RoleAuthority ra) {
        return new RoleAuthorityDTO(
                ra.getId(),
                ra.getRoleCode(),
                ra.getAuthorityName(),
                ra.getCreatedBy(),
                ra.getCreatedDate(),
                ra.getLastModifiedBy(),
                ra.getLastModifiedDate()
        );
    }
}