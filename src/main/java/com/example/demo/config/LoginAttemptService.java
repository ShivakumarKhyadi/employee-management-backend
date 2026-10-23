package com.example.demo.config;

import com.example.demo.model.LoginAttempt;
import com.example.demo.repository.LoginAttemptRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;

    @Autowired
    private LoginAttemptRepository repository;

    @Transactional
    public void loginFailed(String username) {
        LoginAttempt attempt = repository.findById(username).orElseGet(() -> {
            LoginAttempt a = new LoginAttempt();
            a.setUsername(username);
            a.setAttemptCount(0);
            return a;
        });
        attempt.setAttemptCount(attempt.getAttemptCount() + 1);
        attempt.setUpdatedAt(LocalDateTime.now());
        repository.save(attempt);
    }

    @Transactional
    public void loginSucceeded(String username) {
        repository.deleteById(username);
    }

    public boolean isBlocked(String username) {
        return repository.findById(username)
                .map(a -> a.getAttemptCount() >= MAX_ATTEMPTS)
                .orElse(false);
    }


    @Scheduled(fixedRate = 86_400_000)
    @Transactional
    public void purgeStale() {
        repository.deleteByUpdatedAtBefore(LocalDateTime.now().minusDays(1));
    }
}