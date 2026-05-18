package com.cafe.authservice.security.filter;

import com.cafe.authservice.common.exception.CustomException;
import com.cafe.authservice.common.response.CommonResponse;
import com.cafe.authservice.common.response.ErrorCode;
import com.cafe.authservice.repository.UsersRepository;
import com.cafe.authservice.security.jwt.JwtTokenProvider;
import com.cafe.authservice.security.userdetails.CustomUserDetails;
import com.cafe.authservice.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final Long refreshExpiration;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final AuthService authService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String username = obtainUsername(request);
        String password = obtainPassword(request);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password, null);

        return this.getAuthenticationManager().authenticate(token);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();

        authService.updateLastLogin(userDetails.getUuid());

        String uuid = String.valueOf(userDetails.getUuid());
        String role = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_ACCESS_DENIED));

        String accessToken = jwtTokenProvider.createAccessToken(uuid, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(uuid);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // todo 운영환경 true로 변경 필요
                .sameSite("None")
                .maxAge(refreshExpiration / 1000)
                .path("/")
                .build();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json; charset=UTF-8");
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        objectMapper.writeValue(response.getWriter(), CommonResponse.ok(accessToken));
    }
}
