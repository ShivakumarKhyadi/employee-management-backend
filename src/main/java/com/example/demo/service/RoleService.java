package com.example.demo.service;

import com.example.demo.dto.RoleAuthorityDTO;
import com.example.demo.dto.RoleDTO;
import com.example.demo.mapper.RoleMapper;
import com.example.demo.model.Authority;
import com.example.demo.model.Role;
import com.example.demo.model.RoleAuthority;
import com.example.demo.repository.AuthorityRepository;
import com.example.demo.repository.RoleAuthorityRepository;
import com.example.demo.repository.RoleRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleAuthorityRepository roleAuthorityRepository;

    @Autowired
    private AuthorityRepository authorityRepository;


    public record CreateRoleRequest(
            @NotBlank(message = "Role code is required") String roleCode,
            @NotBlank(message = "Role name is required") String roleName,
            List<String> authorityNames
    ) {}

    public record AssignAuthorityRequest(
            @NotBlank(message = "Authority name is required") String authorityName
    ) {}


    public List<RoleDTO> getAllRoles() {

        return roleRepository.findAll().stream().map(RoleMapper::toDto).toList();
    }

    public RoleDTO getRole(String roleCode) {
        return RoleMapper.toDto(findRoleOrThrow(roleCode));
    }

    public List<RoleAuthorityDTO> getAuthoritiesForRole(String roleCode) {
        return roleAuthorityRepository.findByRoleCode(roleCode).stream().map(RoleMapper::toDto).toList();
    }


    @Transactional
    public RoleDTO createRole(CreateRoleRequest request) {
        String code = request.roleCode().trim().toUpperCase();

        if (roleRepository.findByRoleCode(code).isPresent()) {
            throw new IllegalArgumentException("A role with code '" + code + "' already exists");
        }

        List<String> authorityNames = request.authorityNames() != null ? request.authorityNames() : List.of();
        for (String name : authorityNames) {
            if (!authorityRepository.existsById(name)) {
                throw new IllegalArgumentException("No authority named '" + name + "'");
            }
        }

        Role role = new Role();
        role.setRoleCode(code);
        role.setRoleName(request.roleName().trim());
        role = roleRepository.save(role);

        for (String name : authorityNames) {
            RoleAuthority roleAuthority = new RoleAuthority();
            roleAuthority.setRoleCode(code);
            roleAuthority.setAuthorityName(name);
            roleAuthorityRepository.save(roleAuthority);
        }

        return RoleMapper.toDto(role);
    }

    public void deleteRole(String roleCode) {
        Role role = findRoleOrThrow(roleCode);
        roleAuthorityRepository.findByRoleCode(roleCode).forEach(roleAuthorityRepository::delete);
        roleRepository.delete(role);
    }

    public RoleAuthorityDTO assignAuthority(String roleCode, AssignAuthorityRequest request) {
        findRoleOrThrow(roleCode);

        Authority authority = authorityRepository.findById(request.authorityName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No authority named '" + request.authorityName() + "' — create it first under Authorities"));

        boolean alreadyAssigned = roleAuthorityRepository.findByRoleCode(roleCode).stream()
                .anyMatch(ra -> ra.getAuthorityName().equals(authority.getName()));
        if (alreadyAssigned) {
            throw new IllegalArgumentException("Role '" + roleCode + "' already has authority '" + authority.getName() + "'");
        }

        RoleAuthority roleAuthority = new RoleAuthority();
        roleAuthority.setRoleCode(roleCode);
        roleAuthority.setAuthorityName(authority.getName());
        return RoleMapper.toDto(roleAuthorityRepository.save(roleAuthority));
    }

    public void removeAuthority(String roleCode, String authorityName) {
        RoleAuthority match = roleAuthorityRepository.findByRoleCode(roleCode).stream()
                .filter(ra -> ra.getAuthorityName().equals(authorityName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Role '" + roleCode + "' doesn't have authority '" + authorityName + "'"));

        roleAuthorityRepository.delete(match);
    }

    private Role findRoleOrThrow(String roleCode) {
        return roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new IllegalArgumentException("No role with code '" + roleCode + "'"));
    }
}