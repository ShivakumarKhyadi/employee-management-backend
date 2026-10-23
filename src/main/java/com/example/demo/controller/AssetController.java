package com.example.demo.controller;

import com.example.demo.dto.CompanyAssetDTO;
import com.example.demo.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    @Autowired private AssetService assetService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<CompanyAssetDTO>> listAll() {
        return ResponseEntity.ok(assetService.listAll());
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or #employeeId == authentication.principal.employeeId")
    public ResponseEntity<List<CompanyAssetDTO>> listForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(assetService.listForEmployee(employeeId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyAssetDTO> create(@Valid @RequestBody AssetService.CreateAssetRequest request) {
        return ResponseEntity.ok(assetService.createAsset(request));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyAssetDTO> assign(@PathVariable Long id, @Valid @RequestBody AssetService.AssignAssetRequest request) {
        return ResponseEntity.ok(assetService.assignAsset(id, request));
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyAssetDTO> returnAsset(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.returnAsset(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }
}