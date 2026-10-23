package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_acknowledgments", uniqueConstraints = @UniqueConstraint(columnNames = {"file_id", "employee_id"}))
@EntityListeners(AuditingEntityListener.class)
@Data
public class DocumentAcknowledgment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "signed_name", nullable = false)
    private String signedName;

    @Column(nullable = false)
    private LocalDateTime acknowledgedDate;
}