package com.example.demo.controller;

import com.example.demo.dto.AcknowledgmentDTO;
import com.example.demo.dto.PendingAcknowledgmentDTO;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.AcknowledgmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/acknowledgments")
public class AcknowledgmentController {

    @Autowired private AcknowledgmentService acknowledgmentService;

    @GetMapping("/file/{fileId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<AcknowledgmentDTO>> getSigners(@PathVariable Long fileId) {
        return ResponseEntity.ok(acknowledgmentService.getSignersForFile(fileId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PendingAcknowledgmentDTO>> getPending(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(acknowledgmentService.getPendingForEmployee(principal.getEmployeeId()));
    }

    @PostMapping("/file/{fileId}/sign")
    public ResponseEntity<AcknowledgmentDTO> sign(
            @PathVariable Long fileId,
            @Valid @RequestBody AcknowledgmentService.SignRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(acknowledgmentService.sign(fileId, principal.getEmployeeId(), request));
    }
}