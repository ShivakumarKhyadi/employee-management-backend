package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Table(name = "company_assets")
@EntityListeners(AuditingEntityListener.class)
@Data
public class CompanyAsset extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String assetName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType assetType;

    @Column(unique = true)
    private String serialNumber;

    @ManyToOne
    @JoinColumn(name = "emp_id")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status = AssetStatus.AVAILABLE;

    private LocalDate assignedDate;
    private LocalDate returnDueDate;
    private LocalDate returnedDate;

    @Column(nullable = false)
    private boolean returnNotified = false;

    @Column(length = 500)
    private String notes;
}
