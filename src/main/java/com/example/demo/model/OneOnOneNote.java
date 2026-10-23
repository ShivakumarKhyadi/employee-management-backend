package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Table(name = "one_on_one_notes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class OneOnOneNote extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "manager_username", nullable = false)
    private String managerUsername;

    @Column(nullable = false)
    private LocalDate meetingDate;

    @Column(length = 2000)
    private String discussionNotes;

    @Column(length = 1000)
    private String actionItems;
}