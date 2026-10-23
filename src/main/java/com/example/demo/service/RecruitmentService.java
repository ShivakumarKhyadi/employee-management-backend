package com.example.demo.service;

import com.example.demo.dto.JobApplicationDTO;
import com.example.demo.dto.JobPostingDTO;
import com.example.demo.exception.JobApplicationNotFoundException;
import com.example.demo.exception.JobPostingNotFoundException;
import com.example.demo.mapper.RecruitmentMapper;
import com.example.demo.model.*;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.JobApplicationRepository;
import com.example.demo.repository.JobPostingRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecruitmentService {

    private static final String RESUME_DIR = "uploads/resumes/";

    @Autowired private JobPostingRepository jobPostingRepository;
    @Autowired private JobApplicationRepository jobApplicationRepository;
    @Autowired private DepartmentRepository departmentRepository;

    public record CreateJobPostingRequest(
            @NotBlank(message = "Title is required") String title,
            Long departmentId,
            String description,
            LocalDate closingDate
    ) {}

    public record CreateApplicationRequest(
            @NotBlank(message = "Candidate name is required") String candidateName,
            @NotBlank(message = "Candidate email is required") String candidateEmail,
            String candidatePhone,
            String notes
    ) {}

    public record UpdateStageRequest(@NotNull ApplicationStage stage, String notes) {}


    public List<JobPostingDTO> getAllPostings() {
        return jobPostingRepository.findAllByOrderByPostedDateDesc().stream()
                .map(j -> RecruitmentMapper.toDto(j, jobApplicationRepository.findByJobPosting_IdOrderByAppliedDateDesc(j.getId()).size()))
                .collect(Collectors.toList());
    }

    public List<JobPostingDTO> getOpenPostings() {
        return jobPostingRepository.findByStatusOrderByPostedDateDesc(JobStatus.OPEN).stream()
                .map(j -> RecruitmentMapper.toDto(j, jobApplicationRepository.findByJobPosting_IdOrderByAppliedDateDesc(j.getId()).size()))
                .collect(Collectors.toList());
    }

    public JobPostingDTO createPosting(CreateJobPostingRequest request) {
        JobPosting posting = new JobPosting();
        posting.setTitle(request.title().trim());
        posting.setDescription(request.description());
        posting.setClosingDate(request.closingDate());
        posting.setStatus(JobStatus.OPEN);
        posting.setPostedDate(LocalDate.now());
        if (request.departmentId() != null) {
            Department dept = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            posting.setDepartment(dept);
        }
        JobPosting saved = jobPostingRepository.save(posting);
        return RecruitmentMapper.toDto(saved, 0);
    }

    public JobPostingDTO closePosting(Long id) {
        JobPosting posting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new JobPostingNotFoundException("Job posting not found"));
        posting.setStatus(JobStatus.CLOSED);
        JobPosting saved = jobPostingRepository.save(posting);
        return RecruitmentMapper.toDto(saved, jobApplicationRepository.findByJobPosting_IdOrderByAppliedDateDesc(id).size());
    }

    public void deletePosting(Long id) {
        if (!jobPostingRepository.existsById(id)) {
            throw new JobPostingNotFoundException("Job posting not found");
        }
        jobPostingRepository.deleteById(id);
    }


    public List<JobApplicationDTO> getApplicationsForPosting(Long jobPostingId) {
        return jobApplicationRepository.findByJobPosting_IdOrderByAppliedDateDesc(jobPostingId).stream()
                .map(RecruitmentMapper::toDto).collect(Collectors.toList());
    }

    public JobApplicationDTO addApplication(Long jobPostingId, CreateApplicationRequest request) {
        JobPosting posting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new JobPostingNotFoundException("Job posting not found"));

        JobApplication application = new JobApplication();
        application.setJobPosting(posting);
        application.setCandidateName(request.candidateName().trim());
        application.setCandidateEmail(request.candidateEmail().trim());
        application.setCandidatePhone(request.candidatePhone());
        application.setNotes(request.notes());
        application.setStage(ApplicationStage.APPLIED);
        application.setAppliedDate(LocalDateTime.now());
        return RecruitmentMapper.toDto(jobApplicationRepository.save(application));
    }

    public JobApplicationDTO updateStage(Long applicationId, UpdateStageRequest request) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new JobApplicationNotFoundException("Application not found"));
        application.setStage(request.stage());
        if (request.notes() != null) application.setNotes(request.notes());
        return RecruitmentMapper.toDto(jobApplicationRepository.save(application));
    }

    public JobApplicationDTO uploadResume(Long applicationId, MultipartFile file) throws IOException {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new JobApplicationNotFoundException("Application not found"));

        Files.createDirectories(Paths.get(RESUME_DIR));
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = Paths.get(RESUME_DIR, storedName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        application.setResumeFilename(file.getOriginalFilename());
        application.setResumePath(target.toString());
        return RecruitmentMapper.toDto(jobApplicationRepository.save(application));
    }

    public JobApplication getApplicationEntity(Long id) {
        return jobApplicationRepository.findById(id)
                .orElseThrow(() -> new JobApplicationNotFoundException("Application not found"));
    }

    public void deleteApplication(Long id) {
        if (!jobApplicationRepository.existsById(id)) {
            throw new JobApplicationNotFoundException("Application not found");
        }
        jobApplicationRepository.deleteById(id);
    }
}