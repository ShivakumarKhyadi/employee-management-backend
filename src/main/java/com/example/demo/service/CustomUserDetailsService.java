package com.example.demo.service;

import com.example.demo.model.AppUser;
import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.RoleAuthorityRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleAuthorityRepository roleAuthorityRepository;
    @Autowired private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<SimpleGrantedAuthority> authorities = roleAuthorityRepository
                .findByRoleCode(user.getRoleCode())
                .stream()
                .map(ra -> new SimpleGrantedAuthority(ra.getAuthorityName()))
                .collect(Collectors.toList());

        Long employeeId = employeeRepository.findByAppUser_UserId(user.getUserId())
                .map(Employee::getEmpId)
                .orElse(null);

        return new CustomUserDetails(
                user.getUsername(),
                user.getPassword(),
                authorities,
                employeeId,
                user.getUserId(),
                user.getRoleCode()
        );
    }
}