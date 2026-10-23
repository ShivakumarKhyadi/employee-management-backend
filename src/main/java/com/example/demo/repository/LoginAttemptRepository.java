package com.example.demo.repository;

import com.example.demo.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, String> {
    void deleteByUpdatedAtBefore(LocalDateTime cutoff);
}