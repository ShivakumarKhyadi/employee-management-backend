package com.example.demo.controller;

import com.example.demo.dto.HolidayDTO;
import com.example.demo.service.HolidayService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/holidays")
public class HolidayController {

    @Autowired private HolidayService holidayService;

    @GetMapping
    public ResponseEntity<List<HolidayDTO>> getAll() {
        return ResponseEntity.ok(holidayService.getAll());
    }

    @GetMapping("/range")
    public ResponseEntity<List<HolidayDTO>> getForRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(holidayService.getForRange(start, end));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HolidayDTO> create(@Valid @RequestBody HolidayService.CreateHolidayRequest request) {
        return ResponseEntity.ok(holidayService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        holidayService.delete(id);
        return ResponseEntity.noContent().build();
    }
}