package com.example.demo.repository;

import com.example.demo.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmail(String email);
    boolean existsByDepartment_DeptId(Long deptId);
    Optional<Employee> findByAppUser_UserId(Integer userId);
    Optional<Employee> findByAppUser_Username(String username);
    Optional<Employee> findById(Long empId);

    List<Employee> findByReportsToIsNullOrderByName();
    List<Employee> findByReportsTo_EmpIdOrderByName(Long managerId);
}