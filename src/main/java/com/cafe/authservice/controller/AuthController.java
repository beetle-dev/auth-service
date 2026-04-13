package com.cafe.authservice.controller;

import com.cafe.authservice.common.response.CommonResponse;
import com.cafe.authservice.domain.Users;
import com.cafe.authservice.security.CustomUserDetails;
import com.cafe.authservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/logout")
    public ResponseEntity<CommonResponse> logout() {

        UUID uuid = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUuid();
        jwtTokenProvider.deleteRefreshToken(uuid);

        return ResponseEntity.ok(CommonResponse.ok());
    }
}
