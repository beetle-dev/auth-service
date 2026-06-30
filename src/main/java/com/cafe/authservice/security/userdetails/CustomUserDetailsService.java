package com.cafe.authservice.security.userdetails;

import com.cafe.authservice.common.exception.CustomException;
import com.cafe.authservice.common.response.ErrorCode;
import com.cafe.authservice.domain.Role;
import com.cafe.authservice.domain.Users;
import com.cafe.authservice.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.cafe.authservice.common.response.ErrorCode.DISABLE_ACCOUNT;
import static com.cafe.authservice.common.response.ErrorCode.PENDING_ACCOUNT;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        if (!user.isActive()) {
            throw new CustomException(DISABLE_ACCOUNT);
        } else if (user.getRole() == Role.PENDING) {
            throw new CustomException(PENDING_ACCOUNT);
        }

        return new CustomUserDetails(user);
    }
}
