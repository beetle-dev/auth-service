package com.cafe.authservice.security;

import org.springframework.data.repository.CrudRepository;

public interface BlacklistedTokenRepository extends CrudRepository<BlacklistedToken, String> {
}
