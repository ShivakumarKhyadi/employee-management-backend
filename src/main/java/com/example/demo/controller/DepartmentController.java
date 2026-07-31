package com.example.demo.controller;

import com.example.demo.model.Department;
import com.example.demo.model.DepartmentStatus;
import com.example.demo.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody Department department){
        return ResponseEntity.ok(departmentService.createDepartment(department));
    }

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments(){
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{deptId}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long deptId){
        return ResponseEntity.ok(departmentService.getDepartmentById(deptId));
    }

    @PutMapping("/{deptId}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long deptId, @Valid @RequestBody Department department){
        return ResponseEntity.ok(departmentService.updateDepartment(deptId,department));
    }

    @DeleteMapping("/{deptId}")
    public ResponseEntity<String> deleteDepartment(@PathVariable Long deptId){
        departmentService.deleteDepartment(deptId);
        return ResponseEntity.ok("Department Deleted Successfully");
    }

    @GetMapping("/meta/statuses")
    public ResponseEntity<DepartmentStatus[]> getStatuses(){
        return ResponseEntity.ok(DepartmentStatus.values());
    }
}
