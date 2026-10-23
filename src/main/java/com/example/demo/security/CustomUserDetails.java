package com.example.demo.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {

    private final Long employeeId;
    private final Integer userId;
    private final String roleCode;

    public CustomUserDetails(String username, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             Long employeeId, Integer userId, String roleCode) {
        super(username, password, authorities);
        this.employeeId = employeeId;
        this.userId = userId;
        this.roleCode = roleCode;
    }

    public Long getEmployeeId() { return employeeId; }
    public Integer getUserId() { return userId; }
    public String getRoleCode() { return roleCode; }
}