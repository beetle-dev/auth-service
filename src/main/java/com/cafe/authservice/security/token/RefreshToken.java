package com.cafe.authservice.security.token;

import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@AllArgsConstructor
@RedisHash(value = "refresh")
public class RefreshToken {

    @Id
    private String uuid;
    private String jti;

    @TimeToLive
    private Long ttl;
}
