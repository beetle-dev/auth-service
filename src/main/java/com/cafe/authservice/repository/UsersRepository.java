package com.cafe.authservice.repository;

import com.cafe.authservice.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long>, JpaSpecificationExecutor<Users> {

    Optional<Users> findByEmail(String email);
}
