package com.example.demo.controller;

import com.example.demo.dto.AppUserDTO;
import com.example.demo.dto.PageResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AppUserDTO>> getAllUsers(
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 10, sort = "userId") Pageable pageable
    ) {
        return ResponseEntity.ok(userService.searchUsers(search, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserService.CreateUserRequest request) {
        userService.createUser(request);
        return ResponseEntity.ok(Map.of("message", "User and employee created"));
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetPassword(@PathVariable Integer id, @Valid @RequestBody UserService.ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PutMapping("/{id}/mobile-number")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateMobileNumber(@PathVariable Integer id, @Valid @RequestBody UserService.UpdateMobileNumberRequest request) {
        userService.updateMobileNumber(id, request);
        return ResponseEntity.ok(Map.of("message", "Mobile number updated"));
    }

    @PostMapping("/grant/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> grantLogin(@PathVariable Long employeeId, @Valid @RequestBody UserService.GrantLoginRequest request) {
        userService.grantLogin(employeeId, request);
        return ResponseEntity.ok(Map.of("message", "Login access granted"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    @PutMapping("/by-employee/{empId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetPasswordByEmployee(@PathVariable Long empId, @Valid @RequestBody UserService.ResetPasswordRequest request) {
        userService.resetPasswordByEmployee(empId, request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PutMapping("/by-employee/{empId}/mobile-number")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateMobileByEmployee(@PathVariable Long empId, @Valid @RequestBody UserService.UpdateMobileNumberRequest request) {
        userService.updateMobileNumberByEmployee(empId, request);
        return ResponseEntity.ok(Map.of("message", "Mobile number updated"));
    }

    @DeleteMapping("/by-employee/{empId}/login")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeLogin(@PathVariable Long empId) {
        userService.removeLogin(empId);
        return ResponseEntity.ok(Map.of("message", "Login access removed"));
    }
}