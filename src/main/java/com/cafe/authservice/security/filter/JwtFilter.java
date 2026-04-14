package com.cafe.authservice.security.filter;

import com.cafe.authservice.common.exception.CustomAuthenticationFailureHandler;
import com.cafe.authservice.common.exception.JwtAuthenticationException;
import com.cafe.authservice.common.response.ErrorCode;
import com.cafe.authservice.domain.Role;
import com.cafe.authservice.domain.Users;
import com.cafe.authservice.security.jwt.JwtClaims;
import com.cafe.authservice.security.jwt.JwtTokenProvider;
import com.cafe.authservice.security.jwt.PermitAuthPath;
import com.cafe.authservice.security.userdetails.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
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
            failureHandler.onAuthenticationFailure(request, response, new JwtAuthenticationException(ErrorCode.AUTH_TOKEN_INVALID.getMessage()));
            return;
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
            return;
        } catch (Exception e) {
            failureHandler.onAuthenticationFailure(request, response, new JwtAuthenticationException(ErrorCode.AUTH_TOKEN_INVALID.getMessage()));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
