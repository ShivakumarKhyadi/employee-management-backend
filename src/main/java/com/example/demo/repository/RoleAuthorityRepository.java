package com.example.demo.repository;

import com.example.demo.model.RoleAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleAuthorityRepository extends JpaRepository<RoleAuthority, Long> {
    List<RoleAuthority> findByRoleCode(String roleCode);
}
