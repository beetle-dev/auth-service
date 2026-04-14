package com.cafe.authservice.security.jwt;

import com.cafe.authservice.common.exception.CustomAuthenticationFailureHandler;
import com.cafe.authservice.common.exception.CustomException;
import com.cafe.authservice.common.exception.JwtAuthenticationException;
import com.cafe.authservice.common.response.CommonResponse;
import com.cafe.authservice.common.response.ErrorCode;
import com.cafe.authservice.security.token.BlacklistedToken;
import com.cafe.authservice.security.token.BlacklistedTokenRepository;
import com.cafe.authservice.security.token.RefreshToken;
import com.cafe.authservice.security.token.RefreshTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
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
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final ObjectMapper objectMapper;
    private final CustomAuthenticationFailureHandler failureHandler;

    public JwtTokenProvider(@Value("${spring.jwt.secretkey}") String secretKey,
                            @Value("${spring.jwt.access_expiration}") Long accessExpiration,
                            @Value("${spring.jwt.refresh_expiration}") Long refreshExpiration,
                            RefreshTokenRepository refreshTokenRepository,
                            BlacklistedTokenRepository blacklistedTokenRepository,
                            ObjectMapper objectMapper,
                            CustomAuthenticationFailureHandler failureHandler) {
        this.secretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.refreshTokenRepository = refreshTokenRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.objectMapper = objectMapper;
        this.failureHandler = failureHandler;
    }

    public String createAccessToken(String uuid, String name, String role) {

        return Jwts.builder()
                .subject(uuid)
                .id(UUID.randomUUID().toString())
                .claim("name", name)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date((System.currentTimeMillis() + accessExpiration)))
                .signWith(secretKey)
                .compact();
    }

    public JwtClaims validateAccessToken(String accessToken) {

        var payload = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(accessToken).getPayload();

        String jti = payload.getId();

        if (blacklistedTokenRepository.existsById(jti)) {
            throw new CustomException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        return JwtClaims.builder()
                .uuid(UUID.fromString(payload.getSubject()))
                .name(payload.get("name", String.class))
                .role(payload.get("role", String.class))
                .jti(jti)
                .build();
    }

    public void blacklistAccessToken(String accessToken) {

        var payload = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(accessToken).getPayload();

        long ttlSeconds = (payload.getExpiration().getTime() - System.currentTimeMillis()) / 1000;

        if (ttlSeconds > 0) {
            blacklistedTokenRepository.save(new BlacklistedToken(payload.getId(), ttlSeconds));
        }
    }

    public JwtClaims validateRefreshToken(HttpServletRequest request, HttpServletResponse response, String refreshToken) throws ServletException, IOException {

        var payload = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(refreshToken).getPayload();

        boolean exists = refreshTokenRepository.findById(payload.getSubject()).isPresent();

        if (!exists) {
            failureHandler.onAuthenticationFailure(request, response,
                    new BadCredentialsException(ErrorCode.AUTH_TOKEN_INVALID.getMessage()));
            return null;
        }

        return JwtClaims.builder()
                .uuid(UUID.fromString(payload.getSubject()))
                .jti(payload.getId())
                .build();
    }

    public String createRefreshToken(String uuid) {

        refreshTokenRepository.findById(uuid)
                .ifPresent(token -> refreshTokenRepository.deleteById(uuid));

        String jti = UUID.randomUUID().toString();

        refreshTokenRepository.save(new RefreshToken(uuid, jti));

        return Jwts.builder()
                .subject(uuid)
                .id(jti)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date((System.currentTimeMillis() + refreshExpiration)))
                .signWith(secretKey)
                .compact();
    }

    public void reissueAccessToken(HttpServletRequest request, HttpServletResponse response, String name, String role) throws IOException, ServletException {

        String refreshToken = getRefreshToken(request);

        try {
            JwtClaims jwtClaims = validateRefreshToken(request, response, refreshToken);
            if (jwtClaims == null) return ;
            String accessToken = createAccessToken(String.valueOf(jwtClaims.getUuid()), name, role);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json; charset=UTF-8");

            objectMapper.writeValue(response.getWriter(), CommonResponse.reissueAccessToken(accessToken));
        } catch (ExpiredJwtException e) {
            failureHandler.onAuthenticationFailure(request, response, new JwtAuthenticationException(ErrorCode.AUTH_TOKEN_EXPIRE.getMessage()));
        } catch (Exception e) {
            failureHandler.onAuthenticationFailure(request, response, new JwtAuthenticationException(ErrorCode.AUTH_TOKEN_INVALID.getMessage()));
        }
    }

    public void deleteRefreshToken(String uuid) {
        refreshTokenRepository.deleteById(uuid);
    }

    private String getRefreshToken(HttpServletRequest request) {

        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("refreshToken"))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
