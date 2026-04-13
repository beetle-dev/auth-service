package com.cafe.authservice.dto;

import com.cafe.authservice.domain.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
public class UserResDto {

    private Long id;
    private String email;
    private String name;
    private boolean isActive;
    private LocalDateTime lastLoginAt;

    public static UserResDto from(Users user) {

        return UserResDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .isActive(user.isActive())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
