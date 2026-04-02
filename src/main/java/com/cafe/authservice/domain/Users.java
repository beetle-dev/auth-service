package com.cafe.authservice.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Users extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean isActive;

    private LocalDateTime lastLoginAt;

    @PrePersist
    protected void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID();
        if (this.role == null) this.role = Role.STAFF;
        this.isActive = true;
    }
}
