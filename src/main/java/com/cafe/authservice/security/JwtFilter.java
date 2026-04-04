package com.cafe.authservice.security;

import com.cafe.authservice.common.exception.CustomAuthenticationFailureHandler;
import com.cafe.authservice.common.response.ErrorCode;
import com.cafe.authservice.domain.Role;
import com.cafe.authservice.domain.Users;
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

        JwtClaims jwtClaims = jwtTokenProvider.validateAccessToken(accessToken);

        Users user = Users.builder()
                .uuid(jwtClaims.getUuid())
                .name(jwtClaims.getName())
                .role(Role.valueOf(jwtClaims.getRole().replace("ROLE_","")))
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // todo 만료 토큰, 유효X 토큰 예외 처리

        filterChain.doFilter(request, response);
    }
}
