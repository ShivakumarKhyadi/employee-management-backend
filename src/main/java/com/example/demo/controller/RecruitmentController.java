package com.example.demo.controller;

import com.example.demo.dto.JobApplicationDTO;
import com.example.demo.dto.JobPostingDTO;
import com.example.demo.model.JobApplication;
import com.example.demo.service.RecruitmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/recruitment")
public class RecruitmentController {

    @Autowired private RecruitmentService recruitmentService;

    @GetMapping("/postings")
    public ResponseEntity<List<JobPostingDTO>> getPostings(@RequestParam(required = false) boolean openOnly) {
        return ResponseEntity.ok(openOnly ? recruitmentService.getOpenPostings() : recruitmentService.getAllPostings());
    }

    @PostMapping("/postings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobPostingDTO> createPosting(@Valid @RequestBody RecruitmentService.CreateJobPostingRequest request) {
        return ResponseEntity.ok(recruitmentService.createPosting(request));
    }

    @PutMapping("/postings/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobPostingDTO> closePosting(@PathVariable Long id) {
        return ResponseEntity.ok(recruitmentService.closePosting(id));
    }

    @DeleteMapping("/postings/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePosting(@PathVariable Long id) {
        recruitmentService.deletePosting(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/postings/{jobPostingId}/applications")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<JobApplicationDTO>> getApplications(@PathVariable Long jobPostingId) {
        return ResponseEntity.ok(recruitmentService.getApplicationsForPosting(jobPostingId));
    }

    @PostMapping("/postings/{jobPostingId}/applications")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<JobApplicationDTO> addApplication(
            @PathVariable Long jobPostingId, @Valid @RequestBody RecruitmentService.CreateApplicationRequest request) {
        return ResponseEntity.ok(recruitmentService.addApplication(jobPostingId, request));
    }

    @PutMapping("/applications/{id}/stage")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<JobApplicationDTO> updateStage(
            @PathVariable Long id, @Valid @RequestBody RecruitmentService.UpdateStageRequest request) {
        return ResponseEntity.ok(recruitmentService.updateStage(id, request));
    }

    @PostMapping(value = "/applications/{id}/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<JobApplicationDTO> uploadResume(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(recruitmentService.uploadResume(id, file));
    }

    @GetMapping("/applications/{id}/resume/download")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long id) throws IOException {
        JobApplication application = recruitmentService.getApplicationEntity(id);
        Path path = Path.of(application.getResumePath());
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + application.getResumeFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/applications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        recruitmentService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}