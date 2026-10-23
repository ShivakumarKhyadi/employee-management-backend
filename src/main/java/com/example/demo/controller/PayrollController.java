package com.example.demo.controller;

import com.example.demo.dto.PayrollEntryDTO;
import com.example.demo.dto.PayrollRunDTO;
import com.example.demo.dto.SalaryComponentDTO;
import com.example.demo.model.SalaryComponent;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    @Autowired private PayrollService payrollService;

    public record OverrideRequest(Long componentId, Double overrideValue) {}

    @PostMapping("/components")
    @PreAuthorize("hasAuthority('MANAGE_PAYROLL')")
    public ResponseEntity<SalaryComponentDTO> createComponent(@RequestBody SalaryComponent component) {
        return ResponseEntity.ok(payrollService.createComponent(component));
    }

    @GetMapping("/components")
    @PreAuthorize("hasAuthority('MANAGE_PAYROLL')")
    public ResponseEntity<List<SalaryComponentDTO>> listComponents() {
        return ResponseEntity.ok(payrollService.listComponents());
    }

    @DeleteMapping("/components/{id}")
    @PreAuthorize("hasAuthority('MANAGE_PAYROLL')")
    public ResponseEntity<Void> deactivateComponent(@PathVariable Long id) {
        payrollService.deactivateComponent(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/employees/{employeeId}/override")
    @PreAuthorize("hasAuthority('MANAGE_PAYROLL')")
    public ResponseEntity<Void> setOverride(@PathVariable Long employeeId, @RequestBody OverrideRequest request) {
        payrollService.setEmployeeOverride(employeeId, request.componentId(), request.overrideValue());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/run")
    @PreAuthorize("hasAuthority('MANAGE_PAYROLL')")
    public ResponseEntity<PayrollRunDTO> runPayroll(@RequestParam String month, Authentication auth) {
        return ResponseEntity.ok(payrollService.runPayroll(month, auth.getName()));
    }

    @GetMapping("/runs")
    @PreAuthorize("hasAuthority('MANAGE_PAYROLL')")
    public ResponseEntity<List<PayrollRunDTO>> listRuns() {
        return ResponseEntity.ok(payrollService.listRuns());
    }

    @GetMapping("/runs/{runId}/entries")
    @PreAuthorize("hasAuthority('MANAGE_PAYROLL')")
    public ResponseEntity<List<PayrollEntryDTO>> listEntries(@PathVariable Long runId) {
        return ResponseEntity.ok(payrollService.listEntriesForRun(runId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PayrollEntryDTO>> myPayroll(Authentication auth) {
        Long employeeId = (auth.getPrincipal() instanceof CustomUserDetails cud) ? cud.getEmployeeId() : null;
        if (employeeId == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(payrollService.listForEmployee(employeeId));
    }
}