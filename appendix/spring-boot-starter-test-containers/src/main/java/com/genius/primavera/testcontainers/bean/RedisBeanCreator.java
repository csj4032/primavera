package com.genius.primavera.testcontainers.bean;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.RedisContainerSpec;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public class RedisBeanCreator implements BeanCreator {
    
    @Override
    public Object createBean(ContainerInfo containerInfo) {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(containerInfo.getHost());
        redisConfig.setPort(containerInfo.getMappedPort());
        
        // Redis 비밀번호 처리
        if (containerInfo.spec() instanceof RedisContainerSpec redisSpec && redisSpec.getPassword() != null) {
            redisConfig.setPassword(redisSpec.getPassword());
        }
        
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(redisConfig);
        connectionFactory.afterPropertiesSet();
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        
        return template;
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.REDIS;
    }
}