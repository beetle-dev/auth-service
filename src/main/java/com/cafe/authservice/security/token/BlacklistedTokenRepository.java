package com.cafe.authservice.security.token;

import org.springframework.data.repository.CrudRepository;

public interface BlacklistedTokenRepository extends CrudRepository<BlacklistedToken, String> {
}
