package com.example.demo.repository;

import com.example.demo.model.LeaveRequest;
import com.example.demo.model.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeIdOrderByAppliedDateDesc(Long employeeId);
    List<LeaveRequest> findByStatusOrderByAppliedDateAsc(LeaveStatus status);
    List<LeaveRequest> findAllByOrderByAppliedDateDesc();
    List<LeaveRequest> findByEmployeeIdAndStatusAndStartDateBetween(
            Long employeeId, LeaveStatus status, LocalDate rangeStart, LocalDate rangeEnd);

    @Query("SELECT l FROM LeaveRequest l WHERE l.status = :status AND l.startDate <= :end AND l.endDate >= :start")
    List<LeaveRequest> findApprovedOverlapping(@Param("status") LeaveStatus status, @Param("start") LocalDate start, @Param("end") LocalDate end);
}