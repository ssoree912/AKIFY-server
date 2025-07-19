package com.jammering.akkipy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "RefreshToken:";


    public void setValues(String key, String value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    public void setValuesWithTimeUnit(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public String getValues(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public String getRefreshToken(String userId) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
    }

    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

    public boolean checkExistsValue(String value) {
        return value != null && !value.isBlank();
    }
}

