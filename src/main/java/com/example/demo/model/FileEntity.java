package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@EntityListeners(AuditingEntityListener.class)
@Data
public class FileEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String path;

    @ManyToOne
    @JoinColumn(name = "emp_id", nullable = false)
    private Employee employee;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "issue_date")
    private java.time.LocalDate issueDate;

    @Column(name = "expiry_date")
    private java.time.LocalDate expiryDate;

    @Column(name = "expiry_notified", nullable = false)
    private boolean expiryNotified = false;

    @Column(name = "requires_acknowledgment", nullable = false)
    private boolean requiresAcknowledgment = false;
}