package com.example.demo.repository;

import com.example.demo.model.JobPosting;
import com.example.demo.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByStatusOrderByPostedDateDesc(JobStatus status);
    List<JobPosting> findAllByOrderByPostedDateDesc();
}