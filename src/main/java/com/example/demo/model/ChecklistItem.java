package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "checklist_items")
@EntityListeners(AuditingEntityListener.class)
@Data
public class ChecklistItem extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "employee_name", nullable = false)
    private String employeeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChecklistCategory category;

    @Column(nullable = false)
    private String taskName;

    @Column(nullable = false)
    private boolean completed = false;

    private LocalDateTime completedDate;
}