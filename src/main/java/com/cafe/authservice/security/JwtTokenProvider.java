package com.cafe.authservice.security;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final Long accessExpiration;

    public JwtTokenProvider(@Value("${spring.jwt.secretkey}")String secretKey,
                            @Value("${spring.jwt.access_expiration}")Long accessExpiration) {
        this.secretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.accessExpiration = accessExpiration;
    }

    public String createAccessToken(UUID uuid, String name, String role) {

        return Jwts.builder()
                .claim("sub", uuid)
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
}
