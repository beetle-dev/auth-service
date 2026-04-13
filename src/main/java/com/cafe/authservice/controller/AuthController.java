package com.cafe.authservice.controller;

import com.cafe.authservice.common.response.CommonResponse;
import com.cafe.authservice.security.CustomUserDetails;
import com.cafe.authservice.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/logout")
    public ResponseEntity<CommonResponse> logout(HttpServletRequest request, HttpServletResponse response) {

        String authorization = request.getHeader("Authorization");
        String accessToken = authorization.split(" ")[1];
        jwtTokenProvider.blacklistAccessToken(accessToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")    // todo msa 구조에서 되나?
                .maxAge(0)
                .path("/")
                .build();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json; charset=UTF-8");
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        String uuid = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUuid().toString();
        jwtTokenProvider.deleteRefreshToken(uuid);

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(CommonResponse.ok());
    }
}
