package com.cafe.authservice.security;

import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@AllArgsConstructor
@RedisHash(value = "refresh", timeToLive = 2592000)
public class RefreshToken {

    @Id
    private String uuid;
    private String jti;
}
