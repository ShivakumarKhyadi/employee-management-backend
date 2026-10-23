package com.example.demo.controller;

import com.example.demo.dto.AnnouncementDTO;
import com.example.demo.service.AnnouncementFeedService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company-feed")
public class AnnouncementFeedController {

    @Autowired private AnnouncementFeedService announcementFeedService;

    @GetMapping
    public ResponseEntity<List<AnnouncementDTO>> getAll() {
        return ResponseEntity.ok(announcementFeedService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnnouncementDTO> create(@Valid @RequestBody AnnouncementFeedService.CreateAnnouncementRequest request) {
        return ResponseEntity.ok(announcementFeedService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnnouncementDTO> update(@PathVariable Long id, @Valid @RequestBody AnnouncementFeedService.CreateAnnouncementRequest request) {
        return ResponseEntity.ok(announcementFeedService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        announcementFeedService.delete(id);
        return ResponseEntity.noContent().build();
    }
}