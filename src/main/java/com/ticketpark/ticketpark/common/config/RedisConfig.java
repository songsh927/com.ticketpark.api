package com.ticketpark.ticketpark.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    // application.yml에서 host, port 값을 주입하기
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    // Redis 연결 팩토리 설정
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Redis 설정 - host와 port가 필요함
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);

        // Lettuce vs Jedis ⇒ Lettuce 선택, Lettuce 라이브러리를 사용해서 Redis에 연결
        // Lettuce는 Jedis보다 성능이 좋고 비동기 처리가 가능함
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    // RedisTemplate 설정
    // RedisTemplate은 DB 서버에 Set, Get, Delete 등을 사용할 수 있음
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        // RedisTemplate는 트랜잭션을 지원함
        // 트랜잭션 안에서 오류가 발생하면 그 작업을 모두 취소함

        // Redis와 통신할 때 사용할 템플릿 설정
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // key, value에 대한 직렬화 방법 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        // hash key, hash value에 대한 직렬화 방법 설정
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
