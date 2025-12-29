package com.ticketpark.ticketpark.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.auth.database}")
    private int authDatabase;

    @Value("${spring.data.redis.queue.database}")
    private int queueDatabase;

    @Bean
    public RedisConnectionFactory authRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        config.setDatabase(authDatabase);
        return new LettuceConnectionFactory(config);
    }

    @Bean(name = {"authRedisTemplate", "redisTemplate"})
    @Primary
    public RedisTemplate<String, Object> authRedisTemplate() {
        return createTemplate(authRedisConnectionFactory());
    }

    @Bean
    public RedisConnectionFactory queueRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);;
        config.setDatabase(queueDatabase);
        return new LettuceConnectionFactory(config);
    }

    @Bean(name = "queueRedisTemplate")
    public RedisTemplate<String, Object> queueRedisTemplate() {
        return createTemplate(queueRedisConnectionFactory());
    }



    private RedisTemplate<String, Object> createTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
