package com.example.demo.controller;

import com.example.demo.dto.ObjectiveDTO;
import com.example.demo.dto.OneOnOneNoteDTO;
import com.example.demo.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    @Autowired private GoalService goalService;

    @GetMapping("/objectives/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or #employeeId == authentication.principal.employeeId")
    public ResponseEntity<List<ObjectiveDTO>> getObjectives(@PathVariable Long employeeId) {
        return ResponseEntity.ok(goalService.getObjectivesForEmployee(employeeId));
    }

    @PostMapping("/objectives/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ObjectiveDTO> createObjective(
            @PathVariable Long employeeId, @Valid @RequestBody GoalService.CreateObjectiveRequest request) {
        return ResponseEntity.ok(goalService.createObjective(employeeId, request));
    }

    @PutMapping("/objectives/{objectiveId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ObjectiveDTO> updateStatus(
            @PathVariable Long objectiveId, @Valid @RequestBody GoalService.UpdateStatusRequest request) {
        return ResponseEntity.ok(goalService.updateObjectiveStatus(objectiveId, request));
    }

    @PutMapping("/key-results/{keyResultId}/progress")
    public ResponseEntity<Void> updateProgress(
            @PathVariable Long keyResultId, @Valid @RequestBody GoalService.UpdateKeyResultRequest request) {
        goalService.updateKeyResultProgress(keyResultId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/objectives/{objectiveId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteObjective(@PathVariable Long objectiveId) {
        goalService.deleteObjective(objectiveId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/notes/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or #employeeId == authentication.principal.employeeId")
    public ResponseEntity<List<OneOnOneNoteDTO>> getNotes(@PathVariable Long employeeId) {
        return ResponseEntity.ok(goalService.getNotesForEmployee(employeeId));
    }

    @PostMapping("/notes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<OneOnOneNoteDTO> addNote(
            @Valid @RequestBody GoalService.CreateNoteRequest request, Authentication authentication) {
        return ResponseEntity.ok(goalService.addNote(authentication.getName(), request));
    }

    @DeleteMapping("/notes/{noteId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Void> deleteNote(@PathVariable Long noteId) {
        goalService.deleteNote(noteId);
        return ResponseEntity.noContent().build();
    }
}