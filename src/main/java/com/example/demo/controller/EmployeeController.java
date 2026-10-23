package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.mapper.EmployeeMapper;
import com.example.demo.model.Employee;
import com.example.demo.model.EmployeeType;
import com.example.demo.model.Grade;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.EmployeeExcelImportService;
import com.example.demo.service.EmployeeExportService;
import com.example.demo.service.EmployeeService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.dto.ImportResultDTO;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired private EmployeeService employeeService;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private EmployeeExcelImportService employeeExcelImportService;
    @Autowired private EmployeeExportService employeeExportService;

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_EMPLOYEES')")
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody Employee employee){
        return ResponseEntity.ok(EmployeeMapper.toDto(employeeService.createEmployee(employee)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<EmployeeDTO>> getAllEmployees(
            EmployeeSearchCriteria criteria,
            @ParameterObject @PageableDefault(size = 10, sort = "empId") Pageable pageable){

        return ResponseEntity.ok(employeeService.searchEmployees(criteria, pageable));
    }

    @GetMapping("/{empId}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long empId){
        return ResponseEntity.ok(EmployeeMapper.toDto(employeeService.getEmployeeById(empId)));
    }

    @PutMapping("/{empId}")
    @PreAuthorize("hasAuthority('EDIT_ALL_PROFILE') or #empId == authentication.principal.employeeId")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable Long empId,@Valid @RequestBody Employee employee){
        return ResponseEntity.ok(EmployeeMapper.toDto(employeeService.updateEmployee(empId,employee)));
    }

    @DeleteMapping("/{empId}")
    @PreAuthorize("hasAuthority('DELETE_EMPLOYEE')")
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

        Map<String, Long> salaryBands = new LinkedHashMap<>();
        salaryBands.put("Below 30k", 0L);
        salaryBands.put("30k - 60k", 0L);
        salaryBands.put("60k - 1L", 0L);
        salaryBands.put("Above 1L", 0L);
        for (Employee e : all) {
            double salary = e.getSalary() != null ? e.getSalary() : 0;
            if (salary < 30000) salaryBands.merge("Below 30k", 1L, Long::sum);
            else if (salary < 60000) salaryBands.merge("30k - 60k", 1L, Long::sum);
            else if (salary < 100000) salaryBands.merge("60k - 1L", 1L, Long::sum);
            else salaryBands.merge("Above 1L", 1L, Long::sum);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", all.size());
        stats.put("totalSupervisors", all.stream().filter(Employee::isSupervisor).count());
        stats.put("byDepartment", byDepartment);
        stats.put("byGrade", byGrade);
        stats.put("salaryBands", salaryBands);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/{id}/photo")
    public ResponseEntity<?> uploadPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws java.io.IOException {
        String uploadDir = "uploads/employee-photos/";
        java.io.File dir = new java.io.File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String filename = id + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        java.nio.file.Path path = java.nio.file.Paths.get(uploadDir + filename);
        java.nio.file.Files.write(path, file.getBytes());

        employeeService.savePhoto(id, filename);

        return ResponseEntity.ok(Map.of("photoFilename", filename));
    }

    @GetMapping("/me")
    public ResponseEntity<EmployeeDTO> getMyProfile(Authentication authentication) {
        Long employeeId = (authentication.getPrincipal() instanceof CustomUserDetails cud)
                ? cud.getEmployeeId() : null;
        if (employeeId == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(EmployeeMapper.toDto(employeeService.getEmployeeById(employeeId)));
    }


    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ImportResultDTO> importEmployees(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(employeeExcelImportService.importFromExcel(file));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<byte[]> exportEmployees(
            EmployeeSearchCriteria criteria,
            @RequestParam(defaultValue = "csv") String format) throws java.io.IOException {

        byte[] data;
        String filename;
        String contentType;

        switch (format.toLowerCase()) {
            case "xlsx" -> {
                data = employeeExportService.exportExcel(criteria);
                filename = "employees.xlsx";
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            }
            case "docx" -> {
                data = employeeExportService.exportWord(criteria);
                filename = "employees.docx";
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }
            case "pdf" -> {
                data = employeeExportService.exportPdf(criteria);
                filename = "employees.pdf";
                contentType = "application/pdf";
            }
            default -> {
                data = employeeExportService.exportCsv(criteria);
                filename = "employees.csv";
                contentType = "text/csv";
            }
        }

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .body(data);
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAuthority('DELETE_EMPLOYEE')")
    public ResponseEntity<BulkActionResultDTO> bulkDelete(@RequestBody BulkDeleteRequest request) {
        return ResponseEntity.ok(employeeService.bulkDelete(request.empIds()));
    }

    @PutMapping("/bulk-department")
    @PreAuthorize("hasAuthority('MANAGE_EMPLOYEES')")
    public ResponseEntity<BulkActionResultDTO> bulkReassignDepartment(@RequestBody BulkDepartmentRequest request) {
        return ResponseEntity.ok(employeeService.bulkReassignDepartment(request.empIds(), request.departmentId()));
    }
}