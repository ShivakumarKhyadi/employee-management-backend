package com.example.demo.repository;

import com.example.demo.model.PayslipRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayslipRecordRepository extends JpaRepository<PayslipRecord, Long> {

    List<PayslipRecord> findByEmployeeIdOrderByPayMonthDesc(Long employeeId);
    Optional<PayslipRecord> findByEmployeeIdAndPayMonth(Long employeeId, String payMonth);
}
