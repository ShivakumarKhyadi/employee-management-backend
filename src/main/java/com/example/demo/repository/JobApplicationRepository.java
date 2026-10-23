package com.example.demo.repository;

import com.example.demo.model.ApplicationStage;
import com.example.demo.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJobPosting_IdOrderByAppliedDateDesc(Long jobPostingId);
    List<JobApplication> findByStageOrderByAppliedDateDesc(ApplicationStage stage);
}