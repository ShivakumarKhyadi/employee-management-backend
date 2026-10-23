package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
@Data
public class LoginAttempt {
    @Id
    @Column(name = "username")
    private String username;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}