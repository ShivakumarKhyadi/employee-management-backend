package com.example.demo.mapper;

import com.example.demo.dto.EmployeeDTO;
import com.example.demo.model.Department;
import com.example.demo.model.Employee;

public class EmployeeMapper {

    public static EmployeeDTO toDto(Employee e) {
        Department dept = e.getDepartment();
        Employee manager = e.getReportsTo();
        return new EmployeeDTO(
                e.getEmpId(),
                e.getName(),
                e.getEmail(),
                dept != null ? dept.getDeptId() : null,
                dept != null ? dept.getDepartmentName() : null,
                e.getSalary(),
                e.getJoiningDate(),
                e.getDateOfBirth(),
                e.isSupervisor(),
                e.getEmployeeType(),
                e.getGrade(),
                e.getDesignation(),
                e.getPhotoFilename(),
                e.getProjectManagerEmail(),
                e.getAppUser() != null ? e.getAppUser().getUsername() : null,
                manager != null ? manager.getEmpId() : null,
                manager != null ? manager.getName() : null,
                e.getCreatedBy(),
                e.getCreatedDate(),
                e.getLastModifiedBy(),
                e.getLastModifiedDate()
        );
    }

    public static void applyToEntity(EmployeeDTO dto, Employee entity, Department department) {
        entity.setName(dto.name());
        entity.setEmail(dto.email());
        entity.setDepartment(department);
        entity.setSalary(dto.salary());
        entity.setJoiningDate(dto.joiningDate());
        entity.setDateOfBirth(dto.dateOfBirth());
        entity.setSupervisor(dto.supervisor());
        entity.setEmployeeType(dto.employeeType());
        entity.setGrade(dto.grade());
        entity.setDesignation(dto.designation());
        entity.setProjectManagerEmail(dto.projectManagerEmail());
    }
}