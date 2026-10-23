package com.example.demo.controller;

import com.example.demo.dto.AuthorityDTO;
import com.example.demo.service.AuthorityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/authorities")
public class AuthorityController {


    @Autowired private AuthorityService authorityService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ROLES')")
    public ResponseEntity<List<AuthorityDTO>> getAllAuthorities() {
        return ResponseEntity.ok(authorityService.getAllAuthorities());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<AuthorityDTO> createAuthority(@Valid @RequestBody AuthorityService.CreateAuthorityRequest request) {
//        String name = request.name().trim().toUpperCase().replace(' ', '_');
//        if (authorityRepository.existsById(name)) {
//            throw new IllegalArgumentException("Authority '" + name + "' already exists");
//        }
//        Authority authority = new Authority();
//        authority.setName(name);
        return ResponseEntity.ok(authorityService.createAuthority(request));
    }
}