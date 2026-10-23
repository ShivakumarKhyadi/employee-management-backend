package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "role_authority", uniqueConstraints = @UniqueConstraint(columnNames = {"role_code", "authority_name"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class RoleAuthority extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code")
    private String roleCode;

    @Column(name = "authority_name")
    private String authorityName;
}