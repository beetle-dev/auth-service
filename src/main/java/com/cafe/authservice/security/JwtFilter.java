package com.cafe.authservice.security;

import com.cafe.authservice.common.exception.CustomAuthenticationFailureHandler;
import com.cafe.authservice.common.response.ErrorCode;
import com.cafe.authservice.domain.Role;
import com.cafe.authservice.domain.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final CustomAuthenticationFailureHandler failureHandler;
    private final JwtTokenProvider jwtTokenProvider;

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        String path = request.getRequestURI();

        return PermitAuthPath.permitAuthPaths.stream()
                .anyMatch(pattern -> antPathMatcher.match(path, pattern));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            failureHandler.onAuthenticationFailure(request, response, new BadCredentialsException(ErrorCode.AUTH_TOKEN_INVALID.getMessage()));
            // todo return; 필요?
        }

        String accessToken = authorization.split(" ")[1];

        try {
            JwtClaims jwtClaims = jwtTokenProvider.validateAccessToken(accessToken);

            Users user = Users.builder()
                    .uuid(jwtClaims.getUuid())
                    .name(jwtClaims.getName())
                    .role(Role.valueOf(jwtClaims.getRole().replace("ROLE_", "")))
                    .build();

            CustomUserDetails userDetails = new CustomUserDetails(user);

            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ExpiredJwtException e) {

            Claims claims = e.getClaims();
            String name = claims.get("name", String.class);
            String role = claims.get("role", String.class);

            jwtTokenProvider.reissueAccessToken(request, response, name, role);
        } catch (Exception e) {
            failureHandler.onAuthenticationFailure(request, response, new BadCredentialsException(ErrorCode.AUTH_TOKEN_INVALID.getMessage()));
        }

        filterChain.doFilter(request, response);
    }
}
