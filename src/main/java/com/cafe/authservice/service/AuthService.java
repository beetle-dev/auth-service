package com.cafe.authservice.service;

import com.cafe.authservice.common.exception.CustomException;
import com.cafe.authservice.common.response.ErrorCode;
import com.cafe.authservice.common.response.PageResponse;
import com.cafe.authservice.domain.Role;
import com.cafe.authservice.domain.Users;
import com.cafe.authservice.dto.*;
import com.cafe.authservice.repository.UsersRepository;
import com.cafe.authservice.repository.UsersSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

import static com.cafe.authservice.common.response.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(AdminCreateUserReqDto newUser, String requesterRole) {

        if (Role.valueOf(requesterRole) != Role.ADMIN && Role.valueOf(requesterRole) != Role.MANAGER) {
            throw new CustomException(AUTH_ACCESS_DENIED);
        }

        if (Role.valueOf(requesterRole) != Role.ADMIN && newUser.getRole() == Role.ADMIN) {
            throw new CustomException(AUTH_ACCESS_DENIED);
        }

        usersRepository.findByEmail(newUser.getEmail())
                .ifPresent(users -> {
                    throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
                });

        Users user = Users.builder()
                .email(newUser.getEmail())
                .password(passwordEncoder.encode(newUser.getPassword()))
                .name(newUser.getName())
                .role(newUser.getRole())
                .isActive(true)
                .build();

        usersRepository.save(user);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResDto> getUsers(UsersSearchDto reqDto) {

        Sort sort = Sort.by(
                Sort.Direction.fromString(reqDto.getDirection()),
                reqDto.getSort()
        );
        Pageable pageable = PageRequest.of(reqDto.getPage(), reqDto.getSize(), sort);

        Page<Users> users = usersRepository.findAll(UsersSpecification.search(reqDto), pageable);

        Page<UserResDto> result = users.map(UserResDto::from);
        return PageResponse.of(result);
    }

    @Transactional
    public void modifyUser(UUID uuid, UserModifyReqDto reqDto, String requesterRole) {

        if (Role.valueOf(requesterRole) != Role.ADMIN && Role.valueOf(requesterRole) != Role.MANAGER) {
            throw new CustomException(AUTH_ACCESS_DENIED);
        }

        Users users = usersRepository.findByUuid(uuid)
                .orElseThrow(() -> new CustomException(NOT_FOUND));

        if (Role.valueOf(requesterRole) != Role.ADMIN &&
                ( reqDto.getRole() == Role.ADMIN || users.getRole() == Role.ADMIN)) {
            throw new CustomException(AUTH_ACCESS_DENIED);
        }

        String encodedPassword = null;

        if (StringUtils.hasText(reqDto.getPassword())) {
          encodedPassword = passwordEncoder.encode(reqDto.getPassword());
        }

        users.modified(reqDto, encodedPassword);
    }

    @Transactional(readOnly = true)
    public UserResDto getUserInfo(UUID uuid) {
        return UserResDto.from(usersRepository.findByUuid(uuid)
                .orElseThrow(() -> new CustomException(NOT_FOUND)));
    }

    @Transactional
    public void updateLastLogin(UUID uuid) {
        usersRepository.findByUuid(uuid)
                .ifPresent(Users::updateLastLoginAt);
    }

    @Transactional
    public void signup(SelfSignupReqDto newUser) {

        usersRepository.findByEmail(newUser.getEmail())
                .ifPresent(users -> {
                    throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
                });

        Users user = Users.builder()
                .email(newUser.getEmail())
                .password(passwordEncoder.encode(newUser.getPassword()))
                .name(newUser.getName())
                .role(Role.PENDING)
                .isActive(true)
                .build();

        usersRepository.save(user);
    }
}
