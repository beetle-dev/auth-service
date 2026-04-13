package com.cafe.authservice.dto;

import com.cafe.authservice.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class UserReqDto {

    @NotEmpty @Email
    private String email;

    @NotEmpty
    private String password;

    @NotEmpty
    private String name;

    private Role role;
}
