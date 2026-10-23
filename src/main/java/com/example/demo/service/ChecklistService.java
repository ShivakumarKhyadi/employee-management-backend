package com.example.demo.service;

import com.example.demo.dto.ChecklistItemDTO;
import com.example.demo.exception.EmployeeNotFoundException;
import com.example.demo.mapper.ChecklistItemMapper;
import com.example.demo.model.ChecklistCategory;
import com.example.demo.model.ChecklistItem;
import com.example.demo.model.Employee;
import com.example.demo.repository.ChecklistItemRepository;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChecklistService {

    @Autowired private EmployeeRepository employeeRepository;

    private static final List<String> ONBOARDING_TASKS = List.of(
            "Grant system login access",
            "Assign laptop / equipment",
            "Complete HR paperwork",
            "Department orientation",
            "Add to payroll"
    );

    private static final List<String> OFFBOARDING_TASKS = List.of(
            "Collect company assets",
            "Revoke system access",
            "Process final payslip",
            "Conduct exit interview",
            "Remove from active directories"
    );

    @Autowired private ChecklistItemRepository checklistItemRepository;

    public void generateOnboardingChecklist(Employee employee) {
        generate(employee, ChecklistCategory.ONBOARDING, ONBOARDING_TASKS);
    }

    public void generateOffboardingChecklist(Employee employee) {
        generate(employee, ChecklistCategory.OFFBOARDING, OFFBOARDING_TASKS);
    }

    private void generate(Employee employee, ChecklistCategory category, List<String> tasks) {
        for (String task : tasks) {
            ChecklistItem item = new ChecklistItem();
            item.setEmployeeId(employee.getEmpId());
            item.setEmployeeName(employee.getName());
            item.setCategory(category);
            item.setTaskName(task);
            item.setCompleted(false);
            checklistItemRepository.save(item);
        }
    }

    public List<ChecklistItemDTO> getChecklist(Long employeeId) {
        return checklistItemRepository.findByEmployeeIdOrderByIdAsc(employeeId).stream()
                .map(ChecklistItemMapper::toDto).collect(Collectors.toList());
    }

    public ChecklistItemDTO toggleComplete(Long itemId) {
        ChecklistItem item = checklistItemRepository.findById(itemId)
                .orElseThrow(() -> new EmployeeNotFoundException("Checklist item not found"));
        item.setCompleted(!item.isCompleted());
        item.setCompletedDate(item.isCompleted() ? LocalDateTime.now() : null);
        return ChecklistItemMapper.toDto(checklistItemRepository.save(item));
    }

    public List<ChecklistItemDTO> getPending(ChecklistCategory category) {
        return checklistItemRepository.findByCategoryAndCompletedFalseOrderByEmployeeIdAsc(category).stream()
                .map(ChecklistItemMapper::toDto).collect(Collectors.toList());
    }

    public int backfillOnboarding() {
        int created = 0;
        for (Employee employee : employeeRepository.findAll()) {
            if (!checklistItemRepository.existsByEmployeeIdAndCategory(employee.getEmpId(), ChecklistCategory.ONBOARDING)) {
                generateOnboardingChecklist(employee);
                created++;
            }
        }
        return created;
    }
}