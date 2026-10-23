package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_presence")
@Data
public class UserPresence {
    @Id
    private String username;

    private LocalDateTime lastSeen;
}