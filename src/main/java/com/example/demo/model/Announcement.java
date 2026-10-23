package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "announcements")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Announcement extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 3000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnouncementCategory category = AnnouncementCategory.GENERAL;

    @Column(nullable = false)
    private boolean pinned = false;
}