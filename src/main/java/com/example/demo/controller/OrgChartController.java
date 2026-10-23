package com.example.demo.controller;

import com.example.demo.dto.OrgNodeDTO;
import com.example.demo.service.OrgChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/org-chart")
public class OrgChartController {

    @Autowired OrgChartService orgChartService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<OrgNodeDTO>> getOrgChart(){
        return ResponseEntity.ok(orgChartService.getOrgChart());
    }
}
