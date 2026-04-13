package com.cafe.authservice.dto;

import com.cafe.authservice.domain.Role;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class UsersSearchReqDto {

    private String email;
    private String name;
    private Role role;

    @Min(0)
    private int page = 0;

    @Max(100)
    private int size = 20;

    private String sort = "createdAt";
    private String direction = "DESC";
}
