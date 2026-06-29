package com.cafe.authservice.dto;

import com.cafe.authservice.domain.Role;
import com.cafe.authservice.domain.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UserResDto {

    private Long id;
    private UUID uuid;
    private String email;
    private String name;
    private Role role;
    private boolean isActive;
    private LocalDateTime lastLoginAt;

    public static UserResDto from(Users user) {

        return UserResDto.builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .isActive(user.isActive())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
