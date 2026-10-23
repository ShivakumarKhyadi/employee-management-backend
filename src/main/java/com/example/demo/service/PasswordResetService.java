package com.example.demo.service;

import com.example.demo.model.AppUser;
import com.example.demo.model.Employee;
import com.example.demo.model.PasswordResetToken;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordResetTokenRepository tokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final int TOKEN_VALID_MINUTES = 30;
    private static final int WELCOME_TOKEN_VALID_MINUTES = 1440;

    @Transactional
    public void initiateReset(String email) {
        if (email == null || email.isBlank()) return;

        Optional<Employee> employeeOpt = employeeRepository.findByEmail(email.trim());
        if (employeeOpt.isEmpty() || employeeOpt.get().getAppUser() == null) {
            return;
        }

        AppUser user = employeeOpt.get().getAppUser();

        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(UUID.randomUUID().toString());
        prt.setUser(user);
        prt.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_VALID_MINUTES));
        prt.setUsed(false);
        tokenRepository.save(prt);

        String link = frontendUrl + "/reset-password?token=" + prt.getToken();
        emailService.sendResetEmail(email.trim(), user.getUsername(), link, TOKEN_VALID_MINUTES);
    }

    @Transactional
    public void initiateAccountSetup(AppUser user, String email) {
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(UUID.randomUUID().toString());
        prt.setUser(user);
        prt.setExpiresAt(LocalDateTime.now().plusMinutes(WELCOME_TOKEN_VALID_MINUTES));
        prt.setUsed(false);
        tokenRepository.save(prt);

        String link = frontendUrl + "/reset-password?token=" + prt.getToken();
        emailService.sendWelcomeEmail(email, user.getUsername(), link, WELCOME_TOKEN_VALID_MINUTES);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.length() < 8 || !newPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must be at least 8 characters and include an uppercase letter");
        }

        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("This reset link is invalid"));

        if (prt.isUsed()) {
            throw new IllegalArgumentException("This reset link has already been used");
        }
        if (prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This reset link has expired. Please request a new one.");
        }

        AppUser user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        prt.setUsed(true);
        tokenRepository.save(prt);
    }
}