package com.example.demo.service;

import com.example.demo.dto.LeaveBalanceDTO;
import com.example.demo.dto.LeaveRequestDTO;
import com.example.demo.mapper.LeaveRequestMapper;
import com.example.demo.model.*;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    private static final Map<LeaveType, Integer> ANNUAL_ALLOWANCE = Map.of(
            LeaveType.SICK, 12,
            LeaveType.CASUAL, 12,
            LeaveType.EARNED, 15,
            LeaveType.UNPAID, 0
    );

    @Autowired private LeaveRequestRepository leaveRequestRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private EmailService emailService;

    public record ApplyLeaveRequest(LeaveType leaveType, LocalDate startDate, LocalDate endDate, String reason) {}
    public record DecisionRequest(String note) {}

    public LeaveRequestDTO applyLeave(Long employeeId, ApplyLeaveRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        if (employee.getProjectManagerEmail() == null || employee.getProjectManagerEmail().isBlank()) {
            throw new IllegalStateException("No project manager is set for this employee. Contact an administrator.");
        }

        LeaveRequest leave = new LeaveRequest();
        leave.setEmployeeId(employeeId);
        leave.setLeaveType(request.leaveType());
        leave.setStartDate(request.startDate());
        leave.setEndDate(request.endDate());
        leave.setReason(request.reason());
        leave.setStatus(LeaveStatus.PENDING);
        leave.setAppliedDate(LocalDateTime.now());

        LeaveRequest savedLeave = leaveRequestRepository.save(leave);

        String pmEmail = employee.getProjectManagerEmail();

        employeeRepository.findByEmail(pmEmail).ifPresent(pmEmployee -> {
            if (pmEmployee.getAppUser() != null) {
                notificationService.notify(
                        pmEmployee.getAppUser().getUsername(),
                        "LEAVE_REQUEST",
                        employee.getName() + " requested " + request.leaveType() + " leave",
                        "/leave-approvals/" + leave.getId()
                );
            }
        });

         emailService.send(pmEmail,
             "Leave request from " + employee.getName(),
             employee.getName() + " has requested " + request.leaveType() + " leave from "
                 + request.startDate() + " to " + request.endDate()
                 + (request.reason() != null ? ". Reason: " + request.reason() : ""));

        return LeaveRequestMapper.toDto(savedLeave, employee.getName());
    }

    public List<LeaveRequestDTO> listForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        String name = employee != null ? employee.getName() : "Unknown";
        return leaveRequestRepository.findByEmployeeIdOrderByAppliedDateDesc(employeeId).stream()
                .map(r -> LeaveRequestMapper.toDto(r, name))
                .collect(Collectors.toList());
    }

    public List<LeaveRequestDTO> listAllForStaff(LeaveStatus statusFilter) {
        List<LeaveRequest> requests = statusFilter != null
                ? leaveRequestRepository.findByStatusOrderByAppliedDateAsc(statusFilter)
                : leaveRequestRepository.findAllByOrderByAppliedDateDesc();

        return requests.stream()
                .map(r -> {
                    String name = employeeRepository.findById(r.getEmployeeId())
                            .map(Employee::getName).orElse("Unknown");
                    return LeaveRequestMapper.toDto(r, name);
                })
                .collect(Collectors.toList());
    }

    public LeaveRequestDTO decide(Long leaveId, boolean approve, String decidedByUsername, String note) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("This request has already been decided");
        }

        Employee employee = employeeRepository.findById(leave.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        leave.setStatus(approve ? LeaveStatus.APPROVED : LeaveStatus.REJECTED);
        leave.setDecidedBy(decidedByUsername);
        leave.setDecidedDate(LocalDateTime.now());
        leave.setDecisionNote(note);
        leave = leaveRequestRepository.save(leave);

        if (employee.getAppUser() != null) {
            notificationService.notify(
                    employee.getAppUser().getUsername(),
                    "LEAVE_DECISION",
                    "Your " + leave.getLeaveType() + " leave was " + leave.getStatus().name().toLowerCase(),
                    "/leave/" + leave.getId()
            );
        }

         if (employee.getEmail() != null) {
             emailService.send(employee.getEmail(),
                 "Leave request " + leave.getStatus().name().toLowerCase(),
                 "Your " + leave.getLeaveType() + " leave from " + leave.getStartDate() + " to " + leave.getEndDate()
                     + " has been " + leave.getStatus().name().toLowerCase() + "."
                     + (note != null ? " Note: " + note : ""));
         }

        return LeaveRequestMapper.toDto(leave, employee.getName());
    }

    public LeaveRequestDTO cancel(Long leaveId, Long requestingEmployeeId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (!leave.getEmployeeId().equals(requestingEmployeeId)) {
            throw new IllegalStateException("You can only cancel your own leave requests");
        }
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be cancelled");
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        leave = leaveRequestRepository.save(leave);

        Employee employee = employeeRepository.findById(requestingEmployeeId).orElse(null);
        return LeaveRequestMapper.toDto(leave, employee != null ? employee.getName() : "Unknown");
    }

    public LeaveBalanceDTO getBalance(Long employeeId) {
        int currentYear = LocalDate.now().getYear();
        LocalDate yearStart = LocalDate.of(currentYear, 1, 1);
        LocalDate yearEnd = LocalDate.of(currentYear, 12, 31);

        Map<String, Integer> allowed = new HashMap<>();
        Map<String, Integer> used = new HashMap<>();
        Map<String, Integer> remaining = new HashMap<>();

        for (LeaveType type : LeaveType.values()) {
            int allowance = ANNUAL_ALLOWANCE.get(type);
            List<LeaveRequest> approvedThisYear = leaveRequestRepository
                    .findByEmployeeIdAndStatusAndStartDateBetween(employeeId, LeaveStatus.APPROVED, yearStart, yearEnd)
                    .stream()
                    .filter(r -> r.getLeaveType() == type)
                    .collect(Collectors.toList());

            int usedDays = approvedThisYear.stream()
                    .mapToInt(r -> (int) (ChronoUnit.DAYS.between(r.getStartDate(), r.getEndDate()) + 1))
                    .sum();

            allowed.put(type.name(), allowance);
            used.put(type.name(), usedDays);
            remaining.put(type.name(), Math.max(0, allowance - usedDays));
        }

        return new LeaveBalanceDTO(allowed, used, remaining);
    }
}