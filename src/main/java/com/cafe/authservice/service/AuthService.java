package com.cafe.authservice.service;

import com.cafe.authservice.common.exception.CustomException;
import com.cafe.authservice.common.response.ErrorCode;
import com.cafe.authservice.common.response.PageResponse;
import com.cafe.authservice.domain.Role;
import com.cafe.authservice.domain.Users;
import com.cafe.authservice.dto.UserCreateReqDto;
import com.cafe.authservice.dto.UserModifyReqDto;
import com.cafe.authservice.dto.UserResDto;
import com.cafe.authservice.dto.UsersSearchDto;
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

import java.util.Objects;
import java.util.UUID;

import static com.cafe.authservice.common.response.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(UserCreateReqDto newUser) {

        usersRepository.findByEmail(newUser.getEmail())
                .ifPresent(users -> {
                    throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
                });

        Users user = Users.builder()
                .email(newUser.getEmail())
                .password(passwordEncoder.encode(newUser.getPassword()))
                .name(newUser.getName())
                .role(newUser.getRole())
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
    public void modifyUser(UUID uuid, UserModifyReqDto reqDto, String requesterId, String requesterRole) {

        if (!Objects.equals(requesterRole, Role.ADMIN.toString()) && !requesterId.equals(uuid)){
            throw new CustomException(AUTH_ACCESS_DENIED);
        }

        Users users = usersRepository.findByUuid(uuid)
                .orElseThrow(() -> new CustomException(NOT_FOUND));

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
}
