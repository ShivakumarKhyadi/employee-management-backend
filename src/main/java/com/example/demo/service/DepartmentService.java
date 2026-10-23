package com.example.demo.service;

import com.example.demo.dto.DepartmentDTO;
import com.example.demo.dto.DepartmentSearchCriteria;
import com.example.demo.dto.PageResponse;
import com.example.demo.exception.DepartmentInUseException;
import com.example.demo.exception.EmployeeNotFoundException;
import com.example.demo.mapper.DepartmentMapper;
import com.example.demo.model.Department;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.DepartmentSpecifications;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    @Autowired private DepartmentRepository departmentRepository;

    @Autowired private EmployeeRepository employeeRepository;

    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }

    public List<Department> getAllDepartments(){
        return departmentRepository.findAll();
    }

    public Department getDepartmentById(Long deptId){
        return departmentRepository.findById(deptId)
                .orElseThrow(() -> new EmployeeNotFoundException("Department Not Found By ID " + deptId));
    }

    public Department updateDepartment(Long deptId, Department details){
        Department department = getDepartmentById(deptId);
        department.setDepartmentCode(details.getDepartmentCode());
        department.setDepartmentName(details.getDepartmentName());
        department.setStatus(details.getStatus());
        return departmentRepository.save(department);
    }

    public void deleteDepartment(Long id) {
        Department department = getDepartmentById(id);
        if (employeeRepository.existsByDepartment_DeptId(id)) {
            throw new DepartmentInUseException(
                    "Cannot delete '" + department.getDepartmentName() + "' — employees are still assigned to it.");
        }
        departmentRepository.delete(department);
    }

    public PageResponse<DepartmentDTO> searchDepartments(DepartmentSearchCriteria criteria, Pageable pageable) {
        Specification<Department> spec = DepartmentSpecifications.search(criteria);
        Page<Department> result = departmentRepository.findAll(spec, pageable);
        return PageResponse.of(result.map(DepartmentMapper::toDto));
    }
}