package com.example.demo.service;

import com.example.demo.dto.AppUserDTO;
import com.example.demo.dto.PageResponse;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.EmployeeNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.*;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserSpecifications;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@AllArgsConstructor
public class UserService {

     @Autowired private UserRepository userRepository;
     @Autowired private EmployeeRepository employeeRepository;
     @Autowired private DepartmentRepository departmentRepository;
     @Autowired private RoleRepository roleRepository;
     @Autowired private PasswordEncoder passwordEncoder;
     @Autowired private PasswordResetService passwordResetService;



    public record CreateUserRequest(
            @NotBlank(message = "Username is required") String username,
            @NotBlank(message = "Role is required") String role,
            @NotBlank(message = "Mobile number is required for OTP login") String mobileNumber,

            @NotBlank(message = "Employee name is required") String name,
            @NotBlank(message = "Employee email is required") String email,
            @NotNull(message = "Department is required") Long departmentId,
            Double salary,
            LocalDate joiningDate,
            Boolean supervisor,
            EmployeeType employeeType,
            Grade grade
    ) {}

    public record ResetPasswordRequest(
            @NotBlank(message = "New password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter")
            String newPassword
    ) {}

    public record GrantLoginRequest(
            @NotBlank(message = "Username is required") String username,
            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter")
            String password,
            @NotBlank(message = "Role is required") String role,
            @NotBlank(message = "Mobile number is required for OTP login") String mobileNumber
    ) {}


    public record UpdateMobileNumberRequest(
            @NotBlank(message = "Mobile number is required") String mobileNumber
    ) {}

    public PageResponse<AppUserDTO> searchUsers(String search, Pageable pageable) {
        Specification<AppUser> spec = UserSpecifications.search(search);
        Page<AppUser> result = userRepository.findAll(spec, pageable);
        return PageResponse.of(result.map(UserMapper::toDto));
    }


    @Transactional
    public void createUser(CreateUserRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new ConflictException("Username already exists");
        }

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        if (employeeRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException("An employee with this email already exists");
        }

        Role role = roleRepository.findByRoleCode(request.role())
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + request.role()));

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        user.setRole(role);
        user.setMobileNumber(request.mobileNumber());
        user = userRepository.save(user);

        Employee employee = new Employee();
        employee.setName(request.name());
        employee.setEmail(request.email());
        employee.setDepartment(department);
        employee.setSalary(request.salary());
        employee.setJoiningDate(request.joiningDate());
        employee.setSupervisor(request.supervisor() != null && request.supervisor());
        employee.setEmployeeType(request.employeeType());
        employee.setGrade(request.grade());
        employee.setAppUser(user);
        employeeRepository.save(employee);

        passwordResetService.initiateAccountSetup(user, request.email());
    }

    public void resetPassword(Integer id, ResetPasswordRequest request) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public void updateMobileNumber(Integer id, UpdateMobileNumberRequest request) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setMobileNumber(request.mobileNumber());
        userRepository.save(user);
    }

    @Transactional
    public void grantLogin(Long employeeId, GrantLoginRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        if (employee.getAppUser() != null) {
            throw new ConflictException("This employee already has a login");
        }
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new ConflictException("Username already exists");
        }

        Role role = roleRepository.findByRoleCode(request.role())
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + request.role()));

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user.setMobileNumber(request.mobileNumber());
        user = userRepository.save(user);

        employee.setAppUser(user);
        employeeRepository.save(employee);
    }

    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    public void resetPasswordByEmployee(Long employeeId, ResetPasswordRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        AppUser user = employee.getAppUser();
        if (user == null) {
            throw new IllegalArgumentException("This employee has no login account");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public void updateMobileNumberByEmployee(Long employeeId, UpdateMobileNumberRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        AppUser user = employee.getAppUser();
        if (user == null) {
            throw new IllegalArgumentException("This employee has no login account");
        }
        user.setMobileNumber(request.mobileNumber());
        userRepository.save(user);
    }

    @Transactional
    public void removeLogin(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        AppUser user = employee.getAppUser();
        if (user == null) return;
        employee.setAppUser(null);
        employeeRepository.save(employee);
        userRepository.delete(user);
    }
}