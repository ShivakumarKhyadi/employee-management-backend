package com.example.demo.controller;

import com.example.demo.model.Employee;
import com.example.demo.model.EmployeeType;
import com.example.demo.model.Grade;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee){
        return ResponseEntity.ok(employeeService.createEmployee(employee));
    }
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees(){
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{empId}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long empId){
        return ResponseEntity.ok(employeeService.getEmployeeById(empId));
    }

    @PutMapping("/{empId}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long empId,@RequestBody Employee employee){
        return ResponseEntity.ok(employeeService.updateEmployee(empId,employee));
    }

    @DeleteMapping("/{empId}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long empId){
        employeeService.deleteEmployee(empId);
        return ResponseEntity.ok("Employee Deleted Succefully");
    }

    @GetMapping("/meta/types")
    public ResponseEntity<EmployeeType[]> getEmployeeTypes(){
        return ResponseEntity.ok(EmployeeType.values());
    }

    @GetMapping("/meta/grades")
    public ResponseEntity<Grade[]> getGrades(){

        return ResponseEntity.ok(Grade.values());
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<Employee>> getEmployeesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "empId") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(employeeRepository.findAll(pageable));
    }
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<Employee> all = employeeRepository.findAll();

        Map<String, Long> byDepartment = all.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getDepartment() != null ? e.getDepartment().getDepartmentName() : "Unassigned",
                        Collectors.counting()));

        Map<String, Long> byGrade = all.stream()
                .filter(e -> e.getGrade() != null)
                .collect(Collectors.groupingBy(e -> e.getGrade().name(), Collectors.counting()));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", all.size());
        stats.put("totalSupervisors", all.stream().filter(Employee::isSupervisor).count());
        stats.put("byDepartment", byDepartment);
        stats.put("byGrade", byGrade);
        return ResponseEntity.ok(stats);
    }
}
