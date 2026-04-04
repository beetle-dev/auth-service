package com.cafe.authservice.security;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class JwtClaims {
    private UUID uuid;
    private String name;
    private String role;
    private String jti;
}
