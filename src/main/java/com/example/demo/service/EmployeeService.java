package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.exception.EmployeeNotFoundException;
import com.example.demo.mapper.EmployeeMapper;
import com.example.demo.model.Department;
import com.example.demo.model.Employee;
import com.example.demo.model.EmployeeExitLog;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.EmployeeExitLogRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.EmployeeSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeService {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private EmployeeExitLogRepository employeeExitLogRepository;
    @Autowired private ChecklistService checklistService;

    public Employee createEmployee(Employee employee) {
        resolveDepartment(employee);
        resolveReportsTo(employee);
        Employee saved = employeeRepository.save(employee);

        checklistService.generateOnboardingChecklist(saved);

        notificationService.notifyAdminsAndSupervisors(
                "EMPLOYEE_ADDED",
                saved.getName() + " was added to " + saved.getDepartment().getDepartmentName(),
                "/employees/" + saved.getEmpId()
        );
        return saved;
    }

    public List<Employee> getAllEmployees(){
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long empId){
        return employeeRepository.findById(empId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id " + empId));
    }

    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee employee = getEmployeeById(id);
        Long oldDeptId = employee.getDepartment() != null ? employee.getDepartment().getDeptId() : null;

        employee.setName(employeeDetails.getName());
        employee.setEmail(employeeDetails.getEmail());
        employee.setSalary(employeeDetails.getSalary());
        employee.setJoiningDate(employeeDetails.getJoiningDate());
        employee.setSupervisor(employeeDetails.isSupervisor());
        employee.setEmployeeType(employeeDetails.getEmployeeType());
        employee.setGrade(employeeDetails.getGrade());
        employee.setDateOfBirth(employeeDetails.getDateOfBirth());
        employee.setProjectManagerEmail(employeeDetails.getProjectManagerEmail());
        resolveDepartment(employeeDetails);
        employee.setDepartment(employeeDetails.getDepartment());

        employeeDetails.setEmpId(employee.getEmpId());
        resolveReportsTo(employeeDetails);
        employee.setReportsTo(employeeDetails.getReportsTo());

        Employee saved = employeeRepository.save(employee);

        if (oldDeptId != null && !oldDeptId.equals(saved.getDepartment().getDeptId())
                && saved.getAppUser() != null) {
            notificationService.notify(
                    saved.getAppUser().getUsername(),
                    "DEPARTMENT_CHANGED",
                    "You've been moved to " + saved.getDepartment().getDepartmentName(),
                    "/employees/" + saved.getEmpId()
            );
        }

        return saved;

    }

    public void deleteEmployee(Long empId){
        Employee employee = getEmployeeById(empId);

        checklistService.generateOffboardingChecklist(employee);

        EmployeeExitLog exit = new EmployeeExitLog();
        exit.setEmployeeName(employee.getName());
        exit.setDepartmentName(employee.getDepartment() != null ? employee.getDepartment().getDepartmentName() : null);
        exit.setExitDate(LocalDate.now());
        employeeExitLogRepository.save(exit);

        employeeRepository.delete(employee);
    }

    private void resolveDepartment(Employee employee) {
        if (employee.getDepartmentId() == null) {
            throw new IllegalArgumentException("departmentId is required");
        }
        Department department = departmentRepository.findById(employee.getDepartmentId())
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Department not found with id " + employee.getDepartmentId()));
        employee.setDepartment(department);
    }

    public Employee savePhoto(Long id, String filename) {
        Employee employee = getEmployeeById(id);
        employee.setPhotoFilename(filename);
        return employeeRepository.save(employee);
    }

    public PageResponse<EmployeeDTO> searchEmployees(EmployeeSearchCriteria criteria, Pageable pageable) {
        Specification<Employee> spec = EmployeeSpecifications.search(criteria);
        Page<Employee> result = employeeRepository.findAll(spec, pageable);
        return PageResponse.of(result.map(EmployeeMapper::toDto));
    }

    public byte[] exportToCsv(EmployeeSearchCriteria criteria) {
        Specification<Employee> spec = EmployeeSpecifications.search(criteria);
        List<Employee> employees = employeeRepository.findAll(spec);

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Name,Email,Department,Type,Grade,Salary,Joining Date,Supervisor,Has Login\n");

        for (Employee e : employees) {
            EmployeeDTO dto = EmployeeMapper.toDto(e);
            sb.append(csvField(dto.empId())).append(',');
            sb.append(csvField(dto.name())).append(',');
            sb.append(csvField(dto.email())).append(',');
            sb.append(csvField(dto.departmentName())).append(',');
            sb.append(csvField(dto.employeeType() != null ? dto.employeeType().name() : "")).append(',');
            sb.append(csvField(dto.grade() != null ? dto.grade().name() : "")).append(',');
            sb.append(csvField(dto.salary())).append(',');
            sb.append(csvField(dto.joiningDate())).append(',');
            sb.append(csvField(dto.supervisor() ? "Yes" : "No")).append(',');
            sb.append(csvField(dto.loginUsername() != null ? "Yes" : "No")).append('\n');
        }

        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String csvField(Object value) {
        String s = value == null ? "" : String.valueOf(value);
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    public BulkActionResultDTO bulkDelete(List<Long> empIds) {
        List<BulkActionError> errors = new ArrayList<>();
        int succeeded = 0;

        for (Long id : empIds) {
            try {
                deleteEmployee(id);
                succeeded++;
            } catch (Exception ex) {
                errors.add(new BulkActionError(id, ex.getMessage()));
            }
        }
        return new BulkActionResultDTO(empIds.size(), succeeded, errors);
    }

    public BulkActionResultDTO bulkReassignDepartment(List<Long> empIds, Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        List<BulkActionError> errors = new ArrayList<>();
        int succeeded = 0;

        for (Long id : empIds) {
            try {
                Employee employee = getEmployeeById(id);
                Long oldDeptId = employee.getDepartment() != null ? employee.getDepartment().getDeptId() : null;

                employee.setDepartment(department);
                Employee saved = employeeRepository.save(employee);
                succeeded++;

                if (oldDeptId != null && !oldDeptId.equals(department.getDeptId())
                        && saved.getAppUser() != null) {
                    notificationService.notify(
                            saved.getAppUser().getUsername(),
                            "DEPARTMENT_CHANGED",
                            "You've been moved to " + department.getDepartmentName(),
                            "/employees/" + saved.getEmpId()
                    );
                }
            } catch (Exception ex) {
                errors.add(new BulkActionError(id, ex.getMessage()));
            }
        }
        return new BulkActionResultDTO(empIds.size(), succeeded, errors);
    }

    private void resolveReportsTo(Employee employee) {
        if (employee.getReportsToEmpId() == null) {
            employee.setReportsTo(null);
            return;
        }
        if (employee.getReportsToEmpId().equals(employee.getEmpId())) {
            throw new IllegalArgumentException("An employee cannot report to themselves");
        }
        Employee manager = employeeRepository.findById(employee.getReportsToEmpId())
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Manager not found with id " + employee.getReportsToEmpId()));
        employee.setReportsTo(manager);
    }
}