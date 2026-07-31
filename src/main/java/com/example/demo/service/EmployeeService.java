package com.example.demo.service;

import com.example.demo.exception.EmployeeNotFoundException;
import com.example.demo.model.Department;
import com.example.demo.model.Employee;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public Employee createEmployee(Employee employee) {
        resolveDepartment(employee);
        return employeeRepository.save(employee);
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
        employee.setName(employeeDetails.getName());
        employee.setEmail(employeeDetails.getEmail());
        employee.setSalary(employeeDetails.getSalary());
        employee.setJoiningDate(employeeDetails.getJoiningDate());
        employee.setSupervisor(employeeDetails.isSupervisor());
        employee.setEmployeeType(employeeDetails.getEmployeeType());
        employee.setGrade(employeeDetails.getGrade());
        resolveDepartment(employeeDetails);
        employee.setDepartment(employeeDetails.getDepartment());
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long empId){
        Employee employee = getEmployeeById(empId);
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
}
