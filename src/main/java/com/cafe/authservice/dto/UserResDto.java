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
    private String roleCode;
    private String roleName;
    private int roleLevel;
    private boolean isActive;
    private LocalDateTime lastLoginAt;

    public static UserResDto from(Users user) {

        Role role = user.getRole();

        return UserResDto.builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .email(user.getEmail())
                .name(user.getName())
                .roleCode(String.valueOf(role))
                .roleName(role.getRoleName())
                .roleLevel(role.getLevel())
                .isActive(user.isActive())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
