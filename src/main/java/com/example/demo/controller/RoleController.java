package com.example.demo.controller;

import com.example.demo.model.Authority;
import com.example.demo.model.Role;
import com.example.demo.model.RoleAuthority;
import com.example.demo.repository.AuthorityRepository;
import com.example.demo.repository.RoleAuthorityRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.service.RoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dto.RoleDTO;
import com.example.demo.dto.RoleAuthorityDTO;
import com.example.demo.mapper.RoleMapper;


import java.util.List;
import java.util.Map;
@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired private RoleService roleService;


    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ROLES')")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{roleCode}")
    @PreAuthorize("hasAuthority('VIEW_ROLES')")
    public ResponseEntity<RoleDTO> getRole(@PathVariable String roleCode) {
        return ResponseEntity.ok(roleService.getRole(roleCode));
    }


    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    @Transactional
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleService.CreateRoleRequest request) {

        return ResponseEntity.ok(roleService.createRole(request));
    }

    @DeleteMapping("/{roleCode}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<Map<String, String>> deleteRole(@PathVariable String roleCode) {

        roleService.deleteRole(roleCode);
        return ResponseEntity.ok(Map.of("message", "Role deleted successfully"));
    }

    @GetMapping("/{roleCode}/authorities")
    @PreAuthorize("hasAuthority('VIEW_ROLES')")
    public ResponseEntity<List<RoleAuthorityDTO>> getAuthoritiesForRole(@PathVariable String roleCode) {
        return ResponseEntity.ok(
                roleService.getAuthoritiesForRole(roleCode)
        );
    }

    @PostMapping("/{roleCode}/authorities")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<RoleAuthorityDTO> assignAuthority(
            @PathVariable String roleCode,
            @Valid @RequestBody RoleService.AssignAuthorityRequest request
    ) {

        return ResponseEntity.ok(roleService.assignAuthority(roleCode, request));
    }

    @DeleteMapping("/{roleCode}/authorities/{authorityName}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<Map<String, String>> removeAuthority(
            @PathVariable String roleCode,
            @PathVariable String authorityName
    ) {
       roleService.removeAuthority(roleCode, authorityName);
        return ResponseEntity.ok(Map.of("message", "Authority removed from role"));
    }
}