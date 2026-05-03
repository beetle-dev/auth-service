package com.cafe.authservice.domain;

import com.cafe.authservice.dto.UserReqDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
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

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    @PrePersist
    protected void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID();
        if (this.role == null) this.role = Role.PENDING;
        this.isActive = true;
    }

    public void modified(UserReqDto reqDto, PasswordEncoder passwordEncoder) {
        if (StringUtils.hasText(reqDto.getEmail())) this.email = reqDto.getEmail();
        if (StringUtils.hasText(reqDto.getPassword())) this.password = passwordEncoder.encode(reqDto.getPassword());
        if (StringUtils.hasText(reqDto.getName())) this.name = reqDto.getName();
    }
}
