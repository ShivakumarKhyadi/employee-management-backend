package com.example.demo.controller;

import com.example.demo.dto.CelebrationDTO;
import com.example.demo.service.CelebrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/celebrations")
public class CelebrationController {

    @Autowired private CelebrationService celebrationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<CelebrationDTO>> getUpcoming(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(celebrationService.getUpcoming(days));
    }
}