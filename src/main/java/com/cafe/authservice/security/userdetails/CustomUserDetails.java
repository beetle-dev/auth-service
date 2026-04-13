package com.cafe.authservice.security.userdetails;

import com.cafe.authservice.domain.Users;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Users user; // todo final 인 이유?

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().toString())
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    public UUID getUuid() {
        return user.getUuid();
    }

    public String getName() {
        return user.getName();
    }
}
