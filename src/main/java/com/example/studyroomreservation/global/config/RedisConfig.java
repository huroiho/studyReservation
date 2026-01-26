package com.example.studyroomreservation.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession
@ConditionalOnProperty(name = "spring.session.store-type", havingValue = "redis")
public class RedisConfig {


    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        // 1. 잭슨 오브젝트 매퍼 생성
        ObjectMapper objectMapper = new ObjectMapper();

        // 2. 스프링 시큐리티 관련 클래스들을 JSON으로 변환할 수 있게 해주는 모듈 등록
        // 이 설정이 있어야 DefaultCsrfToken 같은 보안 관련 클래스를 읽을 수 있습니다.
        objectMapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}