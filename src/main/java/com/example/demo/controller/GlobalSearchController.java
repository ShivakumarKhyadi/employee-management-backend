package com.example.demo.controller;

import com.example.demo.dto.SearchResultDTO;
import com.example.demo.service.GlobalSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class GlobalSearchController {

    @Autowired private GlobalSearchService globalSearchService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<SearchResultDTO>> search(@RequestParam String q) {
        return ResponseEntity.ok(globalSearchService.search(q));
    }
}