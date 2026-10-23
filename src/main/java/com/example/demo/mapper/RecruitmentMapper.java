package com.example.demo.mapper;

import com.example.demo.dto.JobApplicationDTO;
import com.example.demo.dto.JobPostingDTO;
import com.example.demo.model.JobApplication;
import com.example.demo.model.JobPosting;

public class RecruitmentMapper {

    public static JobPostingDTO toDto(JobPosting j, long applicationCount) {
        return new JobPostingDTO(
                j.getId(), j.getTitle(),
                j.getDepartment() != null ? j.getDepartment().getDeptId() : null,
                j.getDepartment() != null ? j.getDepartment().getDepartmentName() : null,
                j.getDescription(), j.getStatus(), j.getPostedDate(), j.getClosingDate(),
                applicationCount, j.getCreatedBy(), j.getCreatedDate()
        );
    }

    public static JobApplicationDTO toDto(JobApplication a) {
        return new JobApplicationDTO(
                a.getId(), a.getJobPosting().getId(), a.getJobPosting().getTitle(),
                a.getCandidateName(), a.getCandidateEmail(), a.getCandidatePhone(),
                a.getResumeFilename(), a.getStage(), a.getNotes(), a.getAppliedDate()
        );
    }
}