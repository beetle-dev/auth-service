package com.cafe.authservice.security;

import com.cafe.authservice.common.exception.CustomAuthenticationFailureHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement((session) ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(PermitAuthPath.permitAuthPaths.toArray(new String[0])).permitAll()
                        .anyRequest().authenticated())

                .addFilterBefore(new JwtFilter(customAuthenticationFailureHandler, jwtTokenProvider), LoginFilter.class)
                .addFilterAt(new LoginFilter(jwtTokenProvider, objectMapper), UsernamePasswordAuthenticationFilter.class); // todo loginFilter가 bean이 아니니까 jwttokenprovider를 저렇게 주입해야하는건가?

        return http.build();
    }
}
