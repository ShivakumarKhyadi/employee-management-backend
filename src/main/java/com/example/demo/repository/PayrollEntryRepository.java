package com.example.demo.repository;

import com.example.demo.model.PayrollEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PayrollEntryRepository extends JpaRepository<PayrollEntry, Long> {
    List<PayrollEntry> findByRunId(Long runId);
    List<PayrollEntry> findByEmployeeIdOrderByIdDesc(Long employeeId);
    Optional<PayrollEntry> findByRunIdAndEmployeeId(Long runId, Long employeeId);
    void deleteByRunId(Long runId);
}