package com.example.demo.service;

import com.example.demo.dto.PayrollEntryDTO;
import com.example.demo.dto.PayrollRunDTO;
import com.example.demo.dto.SalaryComponentDTO;
import com.example.demo.mapper.PayrollMapper;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    @Autowired private SalaryComponentRepository salaryComponentRepository;
    @Autowired private EmployeeSalaryComponentRepository employeeSalaryComponentRepository;
    @Autowired private PayrollRunRepository payrollRunRepository;
    @Autowired private PayrollEntryRepository payrollEntryRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private NotificationService notificationService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    public SalaryComponentDTO createComponent(SalaryComponent component) {
        component.setActive(true);
        return PayrollMapper.toDto(salaryComponentRepository.save(component));
    }

    public List<SalaryComponentDTO> listComponents() {
        return salaryComponentRepository.findAll().stream().map(PayrollMapper::toDto).collect(Collectors.toList());
    }

    public void deactivateComponent(Long id) {
        SalaryComponent c = salaryComponentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Component not found"));
        c.setActive(false);
        salaryComponentRepository.save(c);
    }

    public void setEmployeeOverride(Long employeeId, Long componentId, Double overrideValue) {
        EmployeeSalaryComponent esc = employeeSalaryComponentRepository
                .findByEmployeeIdAndComponentId(employeeId, componentId)
                .orElseGet(EmployeeSalaryComponent::new);
        esc.setEmployeeId(employeeId);
        esc.setComponentId(componentId);
        esc.setOverrideValue(overrideValue);
        employeeSalaryComponentRepository.save(esc);
    }


    private CalculatedPay calculateForEmployee(Employee employee, List<SalaryComponent> components) {
        double employeeBaseSalary = employee.getSalary() != null ? employee.getSalary() : 0.0;

        List<EmployeeSalaryComponent> overrides = employeeSalaryComponentRepository.findByEmployeeId(employee.getEmpId());

        double basic = 0.0;
        for (SalaryComponent c : components) {
            if (c.getName().equalsIgnoreCase("Basic")) {
                Double override = findOverride(overrides, c.getId());
                double percent = override != null ? override : c.getDefaultValue();
                basic = employeeBaseSalary * (percent / 100.0);
            }
        }
        if (basic == 0.0) basic = employeeBaseSalary; // fallback if no "Basic" component is configured at all

        double gross = 0.0;
        double deductions = 0.0;
        List<PayrollEntryDTO.ComponentLine> breakdown = new ArrayList<>();

        for (SalaryComponent c : components) {
            Double override = findOverride(overrides, c.getId());
            double rate = override != null ? override : c.getDefaultValue();

            double amount = c.getCalculationType() == CalculationType.PERCENT_OF_BASIC
                    ? basic * (rate / 100.0)
                    : rate;

            breakdown.add(new PayrollEntryDTO.ComponentLine(c.getName(), c.getComponentType().name(), amount));

            if (c.getComponentType() == ComponentType.EARNING) {
                gross += amount;
            } else {
                deductions += amount;
            }
        }

        double net = gross - deductions;
        return new CalculatedPay(basic, gross, deductions, net, breakdown);
    }

    private Double findOverride(List<EmployeeSalaryComponent> overrides, Long componentId) {
        return overrides.stream()
                .filter(o -> o.getComponentId().equals(componentId))
                .map(EmployeeSalaryComponent::getOverrideValue)
                .filter(v -> v != null)
                .findFirst()
                .orElse(null);
    }

    private record CalculatedPay(double basic, double gross, double deductions, double net, List<PayrollEntryDTO.ComponentLine> breakdown) {}


    public PayrollRunDTO runPayroll(String payMonth, String runByUsername) {
        if (payrollRunRepository.findByPayMonth(payMonth).isPresent()) {
            throw new IllegalStateException("Payroll for " + payMonth + " has already been run");
        }

        List<SalaryComponent> components = salaryComponentRepository.findByActiveTrue();
        if (components.isEmpty()) {
            throw new IllegalStateException("No active salary components configured");
        }

        List<Employee> employees = employeeRepository.findAll();

        PayrollRun run = new PayrollRun();
        run.setPayMonth(payMonth);
        run.setStatus(PayrollStatus.PROCESSED);
        run.setRunBy(runByUsername);
        run.setRunDate(LocalDateTime.now());
        run.setEmployeeCount(employees.size());
        run = payrollRunRepository.save(run);

        for (Employee employee : employees) {
            CalculatedPay pay = calculateForEmployee(employee, components);

            PayrollEntry entry = new PayrollEntry();
            entry.setRunId(run.getId());
            entry.setEmployeeId(employee.getEmpId());
            entry.setBasicPay(pay.basic());
            entry.setGrossPay(pay.gross());
            entry.setTotalDeductions(pay.deductions());
            entry.setNetPay(pay.net());

            try {
                entry.setBreakdownJson(OBJECT_MAPPER.writeValueAsString(pay.breakdown()));
            } catch (Exception ex) {
                entry.setBreakdownJson("[]");
            }
            payrollEntryRepository.save(entry);

            if (employee.getAppUser() != null) {
                notificationService.notify(
                        employee.getAppUser().getUsername(),
                        "PAYROLL_PROCESSED",
                        "Your salary for " + payMonth + " has been processed",
                        "/my-payroll"
                );
            }
        }

        return PayrollMapper.toDto(run);
    }


    public List<PayrollRunDTO> listRuns() {
        return payrollRunRepository.findAllByOrderByPayMonthDesc().stream().map(PayrollMapper::toDto).collect(Collectors.toList());
    }

    public List<PayrollEntryDTO> listEntriesForRun(Long runId) {
        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Payroll run not found"));

        return payrollEntryRepository.findByRunId(runId).stream()
                .map(e -> {
                    String name = employeeRepository.findById(e.getEmployeeId()).map(Employee::getName).orElse("Unknown");
                    return PayrollMapper.toDto(e, name, run.getPayMonth());
                })
                .collect(Collectors.toList());
    }

    public List<PayrollEntryDTO> listForEmployee(Long employeeId) {
        return payrollEntryRepository.findByEmployeeIdOrderByIdDesc(employeeId).stream()
                .map(e -> {
                    String payMonth = payrollRunRepository.findById(e.getRunId()).map(PayrollRun::getPayMonth).orElse("");
                    String name = employeeRepository.findById(employeeId).map(Employee::getName).orElse("Unknown");
                    return PayrollMapper.toDto(e, name, payMonth);
                })
                .collect(Collectors.toList());
    }
}