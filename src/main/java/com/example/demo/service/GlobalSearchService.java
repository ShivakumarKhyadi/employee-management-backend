package com.example.demo.service;

import com.example.demo.dto.SearchResultDTO;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GlobalSearchService {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private RoleRepository roleRepository;

    public List<SearchResultDTO> search(String query) {
        List<SearchResultDTO> results = new ArrayList<>();
        if (query == null || query.isBlank()) return results;
        String term = query.trim().toLowerCase();

        employeeRepository.findAll().stream()
                .filter(e -> e.getName() != null && e.getName().toLowerCase().contains(term))
                .limit(6)
                .forEach(e -> results.add(new SearchResultDTO(
                        "EMPLOYEE", e.getEmpId(), e.getName(),
                        e.getDepartment() != null ? e.getDepartment().getDepartmentName() : "Employee",
                        "/employees/" + e.getEmpId()
                )));

        departmentRepository.findAll().stream()
                .filter(d -> d.getDepartmentName() != null && d.getDepartmentName().toLowerCase().contains(term))
                .limit(4)
                .forEach(d -> results.add(new SearchResultDTO(
                        "DEPARTMENT", d.getDeptId(), d.getDepartmentName(), "Department",
                        "/departments/edit/" + d.getDeptId()
                )));

        roleRepository.findAll().stream()
                .filter(r -> r.getRoleName() != null && r.getRoleName().toLowerCase().contains(term))
                .limit(4)
                .forEach(r -> results.add(new SearchResultDTO(
                        "ROLE", null, r.getRoleName(), "Role",
                        "/roles/" + r.getRoleCode()
                )));

        return results;
    }
}