package com.bookms.security;

import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String PREFIX = "auth:blacklist:";

    private final StringRedisTemplate stringRedisTemplate;

    public void blacklist(String token, Instant expireAt) {
        Duration ttl = Duration.between(Instant.now(), expireAt);
        if (!ttl.isNegative() && !ttl.isZero()) {
            stringRedisTemplate.opsForValue().set(PREFIX + token, "1", ttl);
        }
    }

    public boolean isBlacklisted(String token) {
        Boolean hasKey = stringRedisTemplate.hasKey(PREFIX + token);
        return Boolean.TRUE.equals(hasKey);
    }
}