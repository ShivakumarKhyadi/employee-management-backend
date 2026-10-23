package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Table(name = "holidays")
@EntityListeners(AuditingEntityListener.class)
@Data
public class Holiday extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 300)
    private String description;

    @Column(nullable = false)
    private boolean optional = false;
}