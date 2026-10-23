package com.example.demo.repository;

import com.example.demo.model.DocumentAcknowledgment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentAcknowledgmentRepository extends JpaRepository<DocumentAcknowledgment, Long> {
    List<DocumentAcknowledgment> findByFileId(Long fileId);
    Optional<DocumentAcknowledgment> findByFileIdAndEmployeeId(Long fileId, Long employeeId);
    List<DocumentAcknowledgment> findByEmployeeId(Long employeeId);
}