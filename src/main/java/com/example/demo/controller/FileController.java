package com.example.demo.controller;

import com.example.demo.dto.FileDTO;
import com.example.demo.model.FileEntity;
import com.example.demo.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/employees/{employeeId}/files")
public class FileController {

    @Autowired private FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('MANAGE_ALL_FILES') or #employeeId == authentication.principal.employeeId")
    public ResponseEntity<FileDTO> upload(
            @PathVariable Long employeeId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate issueDate,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate expiryDate,
            Authentication authentication) throws IOException {
        return ResponseEntity.ok(fileService.storeFile(employeeId, file, authentication.getName(), documentType, issueDate, expiryDate));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_ALL_FILES') or #employeeId == authentication.principal.employeeId")
    public ResponseEntity<List<FileDTO>> list(@PathVariable Long employeeId) {
        return ResponseEntity.ok(fileService.getFilesForEmployee(employeeId));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('MANAGE_ALL_FILES') or #employeeId == authentication.principal.employeeId")
    public ResponseEntity<Resource> download(@PathVariable Long employeeId,
                                             @PathVariable Long id) throws IOException {
        FileEntity entity = fileService.getFileEntity(id);
        if (!entity.getEmployee().getEmpId().equals(employeeId)) {
            throw new AccessDeniedException("File does not belong to this employee");
        }

        Path path = Path.of(entity.getPath());
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + entity.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(
                        entity.getType() != null ? entity.getType() : "application/octet-stream"))
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ALL_FILES') or #employeeId == authentication.principal.employeeId")
    public ResponseEntity<Void> delete(@PathVariable Long employeeId,
                                       @PathVariable Long id) throws IOException {
        FileEntity entity = fileService.getFileEntity(id);
        if (!entity.getEmployee().getEmpId().equals(employeeId)) {
            throw new AccessDeniedException("File does not belong to this employee");
        }
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}