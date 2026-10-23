package com.example.demo.controller;

import com.example.demo.dto.ChecklistItemDTO;
import com.example.demo.model.ChecklistCategory;
import com.example.demo.service.ChecklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checklists")
public class ChecklistController {

    @Autowired private ChecklistService checklistService;

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<ChecklistItemDTO>> getChecklist(@PathVariable Long employeeId) {
        return ResponseEntity.ok(checklistService.getChecklist(employeeId));
    }

    @PutMapping("/{itemId}/toggle")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ChecklistItemDTO> toggle(@PathVariable Long itemId) {
        return ResponseEntity.ok(checklistService.toggleComplete(itemId));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<ChecklistItemDTO>> getPending(@RequestParam ChecklistCategory category) {
        return ResponseEntity.ok(checklistService.getPending(category));
    }

    @PostMapping("/backfill-onboarding")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Integer>> backfillOnboarding() {
        int created = checklistService.backfillOnboarding();
        return ResponseEntity.ok(Map.of("employeesUpdated", created));
    }
}