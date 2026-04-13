package com.cafe.authservice.repository;

import com.cafe.authservice.domain.Role;
import com.cafe.authservice.domain.Users;
import com.cafe.authservice.dto.UsersSearchReqDto;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UsersSpecification {

    public static Specification<Users> search(UsersSearchReqDto dto) {
        return Specification.allOf(
                emailContains(dto.getEmail()),
                nameContains(dto.getName()),
                roleEquals(dto.getRole())
        );
    }

    private static Specification<Users> emailContains(String email) {
        return (root, query, cb) ->
                StringUtils.hasText(email)
                        ? cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%")
                        : null;
    }

    private static Specification<Users> nameContains(String name) {
        return (root, query, cb) ->
                StringUtils.hasText(name)
                        ? cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%")
                        : null;
    }

    private static Specification<Users> roleEquals(Role role) {
        return (root, query, cb) ->
                role != null
                        ? cb.equal(root.get("role"), role)
                        : null;
    }
}
