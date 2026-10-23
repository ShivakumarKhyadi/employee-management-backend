package com.example.demo.service;

import com.example.demo.dto.OrgNodeDTO;
import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrgChartService {

    @Autowired EmployeeRepository employeeRepository;

    public List<OrgNodeDTO> getOrgChart(){
        return employeeRepository.findByReportsToIsNullOrderByName().stream()
                .filter(e -> e.getDesignation() != null && !e.getDesignation().isBlank())
                .map(this::buildNode)
                .collect(Collectors.toList());
    }

    private OrgNodeDTO buildNode(Employee employee){
        List<OrgNodeDTO> children = employeeRepository.findByReportsTo_EmpIdOrderByName(employee.getEmpId()).stream()
                .map(this::buildNode)
                .collect(Collectors.toList());

        return new OrgNodeDTO(
            employee.getEmpId(),
                employee.getName(),
                employee.getDepartment() != null ? employee.getDepartment().getDepartmentName() : null,
                employee.getGrade() != null ? employee.getGrade().name() : null,
                employee.getDesignation(),
                children
        );
    }
}
