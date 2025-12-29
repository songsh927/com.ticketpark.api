package com.ticketpark.ticketpark.common.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Component
public class QueueRedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ZSetOperations<String, Object> zSetOps;
    private final ValueOperations<String, Object> valueOps;

    public QueueRedisService(@Qualifier("queueRedisTemplate") RedisTemplate<String, Object> queueRedisTemplate) {
        this.redisTemplate = queueRedisTemplate;
        this.zSetOps = queueRedisTemplate.opsForZSet();
        this.valueOps = queueRedisTemplate.opsForValue();
    }

    public void addQueue(String key, String userId, long score) {
        zSetOps.add(key, userId, score);
    }

    public Long getRank(String key, String userId) {
        return zSetOps.rank(key, userId);
    }

    public Set<Object> getTopOrder(String key, long count) {
        return zSetOps.range(key, 0, count - 1);
    }

    public void removeUser(String key, String userId) {
        zSetOps.remove(key, userId);
    }

    public void setValues(String key, String data, Duration duration) {
        valueOps.set(key, data, duration);
    }

    public Object getValues(String key) {
        return valueOps.get(key);
    }

    public Set<String> getAllKeys(String pattern){
        return redisTemplate.keys(pattern);
    }

    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }
}
