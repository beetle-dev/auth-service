package com.cafe.authservice.service;

import com.cafe.authservice.common.exception.CustomException;
import com.cafe.authservice.common.response.ErrorCode;
import com.cafe.authservice.domain.Users;
import com.cafe.authservice.dto.UserReqDto;
import com.cafe.authservice.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(UserReqDto newUser) {

        usersRepository.findByEmail(newUser.getEmail())
                .ifPresent(users -> {
                    throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
                });

        Users user = Users.builder()
                .email(newUser.getEmail())
                .password(passwordEncoder.encode(newUser.getPassword()))
                .name(newUser.getName())
                .build();

        usersRepository.save(user);
    }
}
