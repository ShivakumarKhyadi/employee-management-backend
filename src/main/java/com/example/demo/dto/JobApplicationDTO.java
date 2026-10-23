package com.example.demo.dto;

import com.example.demo.model.ApplicationStage;

import java.time.LocalDateTime;

public record JobApplicationDTO(
        Long id,
        Long jobPostingId,
        String jobPostingTitle,
        String candidateName,
        String candidateEmail,
        String candidatePhone,
        String resumeFilename,
        ApplicationStage stage,
        String notes,
        LocalDateTime appliedDate
) {}