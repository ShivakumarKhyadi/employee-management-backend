package com.example.demo.controller;

import com.example.demo.dto.DepartmentDTO;
import com.example.demo.dto.DepartmentSearchCriteria;
import com.example.demo.dto.PageResponse;
import com.example.demo.mapper.DepartmentMapper;
import com.example.demo.model.Department;
import com.example.demo.model.DepartmentStatus;
import com.example.demo.service.DepartmentService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_DEPARTMENTS')")
    public ResponseEntity<DepartmentDTO> createDepartment(@Valid @RequestBody Department department){
        return ResponseEntity.ok(DepartmentMapper.toDto(departmentService.createDepartment(department)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<DepartmentDTO>> getAllDepartments(
            DepartmentSearchCriteria criteria,
            @ParameterObject @PageableDefault(size = 10, sort = "deptId") Pageable pageable) {

        return ResponseEntity.ok(departmentService.searchDepartments(criteria, pageable));
    }

    @GetMapping("/{deptId}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Long deptId){
        return ResponseEntity.ok(DepartmentMapper.toDto(departmentService.getDepartmentById(deptId)));
    }

    @PutMapping("/{deptId}")
    @PreAuthorize("hasAuthority('MANAGE_DEPARTMENTS')")
    public ResponseEntity<DepartmentDTO> updateDepartment(@PathVariable Long deptId, @Valid @RequestBody Department department){
        return ResponseEntity.ok(DepartmentMapper.toDto(departmentService.updateDepartment(deptId,department)));
    }

    @DeleteMapping("/{deptId}")
    @PreAuthorize("hasAuthority('MANAGE_DEPARTMENTS')")
    public ResponseEntity<String> deleteDepartment(@PathVariable Long deptId){
        departmentService.deleteDepartment(deptId);
        return ResponseEntity.ok("Department Deleted Successfully");
    }

    @GetMapping("/meta/statuses")
    public ResponseEntity<DepartmentStatus[]> getStatuses(){
        return ResponseEntity.ok(DepartmentStatus.values());
    }
}