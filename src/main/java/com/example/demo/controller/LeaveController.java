package com.example.demo.controller;

import com.example.demo.dto.LeaveBalanceDTO;
import com.example.demo.dto.LeaveRequestDTO;
import com.example.demo.model.LeaveStatus;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveController {

    @Autowired private LeaveService leaveService;

    private Long employeeIdOf(Authentication auth) {
        return (auth.getPrincipal() instanceof CustomUserDetails cud) ? cud.getEmployeeId() : null;
    }

    @PostMapping
    public ResponseEntity<LeaveRequestDTO> apply(
            @RequestBody LeaveService.ApplyLeaveRequest request, Authentication auth) {
        Long employeeId = employeeIdOf(auth);
        if (employeeId == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(leaveService.applyLeave(employeeId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<LeaveRequestDTO>> myRequests(Authentication auth) {
        Long employeeId = employeeIdOf(auth);
        if (employeeId == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(leaveService.listForEmployee(employeeId));
    }

    @GetMapping("/my/balance")
    public ResponseEntity<LeaveBalanceDTO> myBalance(Authentication auth) {
        Long employeeId = employeeIdOf(auth);
        if (employeeId == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(leaveService.getBalance(employeeId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<LeaveRequestDTO>> allRequests(@RequestParam(required = false) LeaveStatus status) {
        return ResponseEntity.ok(leaveService.listAllForStaff(status));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<LeaveRequestDTO> approve(
            @PathVariable Long id, @RequestBody(required = false) LeaveService.DecisionRequest request, Authentication auth) {
        String note = request != null ? request.note() : null;
        return ResponseEntity.ok(leaveService.decide(id, true, auth.getName(), note));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<LeaveRequestDTO> reject(
            @PathVariable Long id, @RequestBody(required = false) LeaveService.DecisionRequest request, Authentication auth) {
        String note = request != null ? request.note() : null;
        return ResponseEntity.ok(leaveService.decide(id, false, auth.getName(), note));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<LeaveRequestDTO> cancel(@PathVariable Long id, Authentication auth) {
        Long employeeId = employeeIdOf(auth);
        if (employeeId == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(leaveService.cancel(id, employeeId));
    }


}