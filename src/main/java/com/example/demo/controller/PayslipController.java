package com.example.demo.controller;

import com.example.demo.dto.PayslipRecordDTO;
import com.example.demo.model.PayslipRecord;
import com.example.demo.service.PayslipService;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/employees/{employeeId}/payslips")
public class PayslipController {

    @Autowired private PayslipService payslipService;

    @GetMapping
    @PreAuthorize("hasAuthority('EDIT_ALL_PROFILE') or #employeeId == authentication.principal.employeeId")
    public ResponseEntity<List<PayslipRecordDTO>> list(@PathVariable Long employeeId){
        return ResponseEntity.ok(payslipService.listForEmployee(employeeId));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('EDIT_ALL_PROFILE')")
    public ResponseEntity<PayslipRecordDTO> generate(
            @PathVariable Long employeeId,
            @RequestParam String month) throws IOException {
        return ResponseEntity.ok(payslipService.generatePayslip(employeeId, month, null));
    }

    @GetMapping("/{payslipId}/download")
    @PreAuthorize("hasAuthority('EDIT_ALL_PROFILE') or #employeeId == authentication.principal.employeeId")
    public ResponseEntity<Resource> download(
            @PathVariable Long employeeId,
            @PathVariable Long payslipId) throws IOException{
        PayslipRecord record = payslipService.getRecord(payslipId);
        Path path = Path.of(record.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"payslip-" + record.getPayMonth() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/{payslipId}/preview")
    @PreAuthorize("hasAuthority('EDIT_ALL_PROFILE') or #employeeId == authentication.principal.employeeId")
    public ResponseEntity<Resource> preview(
            @PathVariable Long employeeId,
            @PathVariable Long payslipId) throws IOException {

        PayslipRecord record = payslipService.getRecord(payslipId);
        Path path = Path.of(record.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"payslip-" + record.getPayMonth() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}
