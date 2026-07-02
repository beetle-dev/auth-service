package com.cafe.authservice.dto;

import com.cafe.authservice.domain.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class AdminCreateUserReqDto {

    @NotEmpty @Email
    private String email;

    @NotEmpty
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "비밀번호는 영문+숫자 조합이어야 합니다.")
    private String password;

    @NotEmpty
    private String name;

    @NotNull
    private Role role;
}
