package com.cafe.authservice.service;

import com.cafe.authservice.common.exception.CustomException;
import com.cafe.authservice.common.response.ErrorCode;
import com.cafe.authservice.common.response.PageResponse;
import com.cafe.authservice.domain.Role;
import com.cafe.authservice.domain.Users;
import com.cafe.authservice.dto.UserReqDto;
import com.cafe.authservice.dto.UserResDto;
import com.cafe.authservice.dto.UsersSearchReqDto;
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

import java.util.List;
import java.util.UUID;

import static com.cafe.authservice.common.response.ErrorCode.*;

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

    public PageResponse<UserResDto> getUsers(UsersSearchReqDto reqDto) {

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
    public void modifyUser(UUID uuid, UserReqDto reqDto, Users currentUser) {

        if (currentUser.getRole() != Role.ADMIN && !currentUser.getUuid().equals(uuid)){
            throw new CustomException(AUTH_ACCESS_DENIED);
        }

        Users users = usersRepository.findByUuid(uuid)
                .orElseThrow(() -> new CustomException(NOT_FOUND));

        usersRepository.findByEmail(reqDto.getEmail())
                        .ifPresent(users1 -> {throw new CustomException(DUPLICATE_EMAIL);});

        users.modified(reqDto, passwordEncoder);
    }

    public Users getUserInfo(UUID uuid) {

        return usersRepository.findByUuid(uuid)
                .orElseThrow(() -> new CustomException(NOT_FOUND));
    }
}
