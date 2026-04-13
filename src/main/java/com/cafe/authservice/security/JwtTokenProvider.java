package com.cafe.authservice.security;

import com.cafe.authservice.common.exception.CustomAuthenticationFailureHandler;
import com.cafe.authservice.common.response.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final Long accessExpiration;
    private final Long refreshExpiration;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper;
    private final CustomAuthenticationFailureHandler failureHandler;

    public JwtTokenProvider(@Value("${spring.jwt.secretkey}")String secretKey,
                            @Value("${spring.jwt.access_expiration}")Long accessExpiration,
                            @Value("${spring.jwt.refresh_expiration}")Long refreshExpiration,
                            RefreshTokenRepository refreshTokenRepository,
                            ObjectMapper objectMapper,
                            CustomAuthenticationFailureHandler failureHandler) {
        this.secretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.refreshTokenRepository = refreshTokenRepository;
        this.objectMapper = objectMapper;
        this.failureHandler = failureHandler;
    }

    public String createAccessToken(String uuid, String name, String role) {

        return Jwts.builder()
                .subject(uuid)
                .claim("name", name)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date((System.currentTimeMillis() + accessExpiration)))
                .signWith(secretKey)
                .compact();
    }

    public JwtClaims validateAccessToken(String accessToken) {

        var payload = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(accessToken).getPayload();

        return JwtClaims.builder()
                .uuid(payload.get("sub", UUID.class))
                .name(payload.get("name", String.class))
                .name(payload.get("roles", String.class))
                .build();
    }

    public JwtClaims validateRefreshToken(HttpServletRequest request, HttpServletResponse response, String refreshToken) throws ServletException, IOException {

        var payload = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(refreshToken).getPayload();

        boolean exists = refreshTokenRepository.findById(String.valueOf(payload.get("sub", UUID.class))).isPresent();

        if (!exists) {
            failureHandler.onAuthenticationFailure(request, response,
                    new BadCredentialsException(ErrorCode.AUTH_TOKEN_INVALID.getMessage()));
            return null;
        }

        return JwtClaims.builder()
                .uuid(payload.get("sub", UUID.class))
                .jti(payload.get("jti", String.class))
                .build();
    }

    public String createRefreshToken(String uuid) {
        return null;
    }

    public void reissueAccessToken(HttpServletRequest request, HttpServletResponse response, String name, String role) throws IOException, ServletException {

        String refreshToken = getRefreshToken(request);

        try {

            JwtClaims jwtClaims = validateRefreshToken(request, response, refreshToken);

            String accessToken = createAccessToken(String.valueOf(jwtClaims.getUuid()), name, role);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json; charset=UTF-8");

            objectMapper.writeValue(response.getWriter(), "Bearer " + accessToken);
        } catch (ExpiredJwtException e) {
            failureHandler.onAuthenticationFailure(request, response, new BadCredentialsException(ErrorCode.AUTH_TOKEN_EXPIRE.getMessage()));
        } catch (Exception e) {
            failureHandler.onAuthenticationFailure(request, response, new BadCredentialsException(ErrorCode.AUTH_TOKEN_INVALID.getMessage()));
        }
    }

    private String getRefreshToken(HttpServletRequest request) {

        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .map(Cookie::getValue)
                .filter(value -> value.equals("refreshToken"))
                .findFirst()
                .orElse(null);
    }

    public void deleteRefreshToken(UUID uuid) {


    }
}