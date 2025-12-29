package com.ticketpark.ticketpark.common.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, Object> values;


    public AuthRedisService(@Qualifier("authRedisTemplate") RedisTemplate<String, Object> authRedisTemplate) {
        this.redisTemplate = authRedisTemplate;
        this.values = redisTemplate.opsForValue();
    }

    // 기본 데이터 저장
    public void setValues(String key, String data) {
        values.set(key, data);
    }

    public void setValues(String key, String data, Duration duration) {
        values.set(key, data, duration);
    }

    public Object getValues(String key) {
        return values.get(key);
    }

    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

}
