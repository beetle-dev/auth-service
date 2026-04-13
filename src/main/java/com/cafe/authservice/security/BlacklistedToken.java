package com.cafe.authservice.security;

import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@AllArgsConstructor
@RedisHash(value = "blacklist")
public class BlacklistedToken {

    @Id
    private String jti;

    @TimeToLive
    private Long ttl;
}
