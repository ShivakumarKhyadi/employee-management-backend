package com.example.demo.controller;

import com.example.demo.config.JwtUtil;
import com.example.demo.config.TokenBlacklistService;
import com.example.demo.model.AppUser;
import com.example.demo.model.Employee;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.UserSession;
import com.example.demo.repository.*;
import com.example.demo.service.OtpService;
import com.example.demo.service.PasswordResetService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.example.demo.config.LoginAttemptService;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository userRepository;
    @Autowired EmployeeRepository employeeRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private TokenBlacklistService tokenBlacklistService;

    public record LoginRequest(String username, String password) {}

    @Autowired private UserSessionRepository sessionRepository;
    @Autowired private LoginAttemptService loginAttemptService;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private RoleAuthorityRepository roleAuthorityRepository;
    @Autowired private PasswordResetService passwordResetService;
    @Autowired private OtpService otpService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (loginAttemptService.isBlocked(request.username())) {
            return ResponseEntity.status(429).body(Map.of(
                    "message", "Too many failed attempts. Please try again later."));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(request.username());
            return ResponseEntity.status(401).body(Map.of("message", "Invalid username or password"));
        }

        loginAttemptService.loginSucceeded(request.username());

        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(user.getMobileNumber() == null || user.getMobileNumber().isBlank()){
            return ResponseEntity.status(400).body(Map.of(
                    "message", "No mobile number on file for this account. Contact an administrator to enable OTP login."
            ));
        }

        otpService.generateAndSend(user.getUsername(), user.getMobileNumber());
        String masked = maskMobile(user.getMobileNumber());

        return ResponseEntity.ok(Map.of(
                "message","OTP sent",
                "username", user.getUsername(),
                "maskedMobile", masked
        ));
    }

    public record VerifyOtpRequest(String username, String otp){}

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request){
        boolean valid = otpService.verify(request.username(),request.otp());
        if(!valid){
            return ResponseEntity.status(401).body(Map.of("message", "Invalid or expired OTP"));
        }

        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserSession session = new UserSession();
        session.setUsername(request.username());
        session.setLoginTime(LocalDateTime.now());
        session = sessionRepository.save(session);

        String token = jwtUtil.generateToken(request.username(), session.getSessionId());
        String refreshToken = createRefreshToken(request.username(), session.getSessionId());

        List<String> authorities = roleAuthorityRepository
                .findByRoleCode(user.getRole().getRoleCode())
                .stream()
                .map(ra -> ra.getAuthorityName())
                .collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        body.put("refreshToken", refreshToken);
        body.put("username", user.getUsername());
        body.put("role", user.getRole().getRoleCode());
        body.put("authorities", authorities);

        Long employeeId = employeeRepository.findByAppUser_UserId(user.getUserId())
                .map(Employee::getEmpId)
                .orElse(null);

        body.put("employeeId", employeeId);

        return ResponseEntity.ok(body);
    }

    public record ResendOtpRequest(String username) {}

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody ResendOtpRequest request){
        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(user.getMobileNumber() == null || user.getMobileNumber().isBlank()){
            return ResponseEntity.status(400).body(Map.of("message", "No mobile number on file for this account."));
        }

        otpService.generateAndSend(user.getUsername(), user.getMobileNumber());
        return  ResponseEntity.ok(Map.of("message", "OTP resent", "maskedMobile", maskMobile(user.getMobileNumber())));
    }

    private String maskMobile(String mobile) {
        if (mobile.length() < 4) return "••••";
        return "•".repeat(mobile.length() - 4) + mobile.substring(mobile.length() - 4);
    }

    @Transactional
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklist(token);

            Long sessionId = jwtUtil.extractSessionId(token);
            if (sessionId != null) {
                sessionRepository.findById(sessionId).ifPresent(session -> {
                    LocalDateTime now = LocalDateTime.now();
                    session.setLogoutTime(now);
                    session.setDurationSeconds(Duration.between(session.getLoginTime(), now).getSeconds());
                    sessionRepository.save(session);
                });
            }
            refreshTokenRepository.deleteByUsername(jwtUtil.extractUsername(token));
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    private String createRefreshToken(String username, Long sessionId) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(java.util.UUID.randomUUID().toString());
        rt.setUsername(username);
        rt.setSessionId(sessionId);
        rt.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(rt);
        return rt.getToken();
    }

    public record RefreshRequest(String refreshToken) {}

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElse(null);

        if (stored == null || stored.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(401).body(Map.of("message", "Refresh token invalid or expired"));
        }

        AppUser user = userRepository.findByUsername(stored.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = stored.getSessionId() != null
                ? jwtUtil.generateToken(user.getUsername(), stored.getSessionId())
                : jwtUtil.generateToken(user.getUsername());

        List<String> authorities = roleAuthorityRepository
                .findByRoleCode(user.getRole().getRoleCode())
                .stream()
                .map(ra -> ra.getAuthorityName())
                .collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put("token", newAccessToken);
        body.put("username", user.getUsername());
        body.put("role", user.getRole().getRoleCode());
        body.put("authorities", authorities);

        Long employeeId = employeeRepository.findByAppUser_UserId(user.getUserId())
                .map(Employee::getEmpId)
                .orElse(null);

        body.put("employeeId", employeeId);
        return ResponseEntity.ok(body);
    }

    public record ForgotPasswordRequest(String email) {}

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.initiateReset(request.email());
        return ResponseEntity.ok(Map.of("message", "If that email is registered, a reset link has been sent."));
    }

    public record ResetPasswordRequest(String token, String newPassword) {}

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.token(), request.newPassword());
            return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    public record LocationUpdateRequest(Double latitude, Double longitude) {}

    @PostMapping("/session-location")
    public ResponseEntity<?> updateSessionLocation(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody LocationUpdateRequest request) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "Missing token"));
        }
        String token = authHeader.substring(7);
        Long sessionId = jwtUtil.extractSessionId(token);
        if (sessionId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "No session associated with this token"));
        }

        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setLatitude(request.latitude());
            session.setLongitude(request.longitude());
            sessionRepository.save(session);
        });

        return ResponseEntity.ok(Map.of("message", "Location recorded"));
    }
}