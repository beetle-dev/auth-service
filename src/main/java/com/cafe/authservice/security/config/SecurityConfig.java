package com.cafe.authservice.security.config;

import com.cafe.authservice.common.exception.CustomAuthenticationFailureHandler;
import com.cafe.authservice.common.response.CommonResponse;
import com.cafe.authservice.common.response.ErrorCode;
import com.cafe.authservice.repository.UsersRepository;
import com.cafe.authservice.security.filter.GatewayAuthFilter;
import com.cafe.authservice.security.filter.LoginFilter;
import com.cafe.authservice.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    @Value("${spring.jwt.refresh_expiration}")
    private Long refreshExpiration;

    private final GatewayAuthFilter gatewayAuthFilter;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final UsersRepository usersRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        LoginFilter loginFilter = new LoginFilter(refreshExpiration, jwtTokenProvider, objectMapper, usersRepository);
        loginFilter.setAuthenticationManager(authenticationManager());
        loginFilter.setAuthenticationFailureHandler(customAuthenticationFailureHandler);
        // todo auth 검증 제외 >> api-gateway로 이관?
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement((session) ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json; charset=UTF-8");
                            objectMapper.writeValue(response.getWriter(),
                                    CommonResponse.fail(ErrorCode.AUTH_TOKEN_INVALID));
                        })
                )

                .authorizeHttpRequests((auth) -> auth.anyRequest().permitAll())

                .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
