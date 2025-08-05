package com.genius.primavera.testcontainer.v4;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ContainerBeanRegistrar {
    
    private final ContainerManager containerManager;
    
    public void registerBeans(ConfigurableListableBeanFactory beanFactory) {
        containerManager.getAllContainers().forEach(containerInfo -> {
            try {
                Object bean = createBean(containerInfo);
                if (bean != null) {
                    beanFactory.registerSingleton(containerInfo.getName(), bean);
                    log.info("Registered {} bean '{}' for container type {}", 
                        bean.getClass().getSimpleName(), containerInfo.getName(), containerInfo.getType());
                }
            } catch (Exception e) {
                log.error("Failed to register bean for container: {}", containerInfo.getName(), e);
            }
        });
    }
    
    private Object createBean(ContainerInfo containerInfo) {
        return switch (containerInfo.getType()) {
            case MARIADB, MYSQL, POSTGRESQL -> createDataSource(containerInfo);
            case REDIS -> createRedisTemplate(containerInfo);
            case KAFKA -> createKafkaTemplate(containerInfo);
            case MONGODB -> createMongoConnectionString(containerInfo);
            case ELASTICSEARCH -> createElasticsearchConfig(containerInfo);
        };
    }
    
    private DataSource createDataSource(ContainerInfo containerInfo) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(containerInfo.getJdbcUrl());
        config.setDriverClassName(containerInfo.getType().getDriverClassName());
        config.setUsername(containerInfo.getSpec().getUsernameOrDefault());
        config.setPassword(containerInfo.getSpec().getPasswordOrDefault());
        
        config.setPoolName(containerInfo.getName() + "-pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        
        return new HikariDataSource(config);
    }
    
    private RedisTemplate<String, Object> createRedisTemplate(ContainerInfo containerInfo) {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(containerInfo.getHost());
        redisConfig.setPort(containerInfo.getMappedPort());
        
        String password = containerInfo.getSpec().getPassword();
        if (password != null && !password.isEmpty()) {
            redisConfig.setPassword(password);
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
    
    private KafkaTemplate<String, Object> createKafkaTemplate(ContainerInfo containerInfo) {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", containerInfo.getConnectionString());
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.springframework.kafka.support.serializer.JsonSerializer");
        props.put("acks", "all");
        props.put("retries", 3);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        
        ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(producerFactory);
    }
    
    private String createMongoConnectionString(ContainerInfo containerInfo) {
        return containerInfo.getConnectionString();
    }
    
    private Map<String, Object> createElasticsearchConfig(ContainerInfo containerInfo) {
        Map<String, Object> config = new HashMap<>();
        config.put("host", containerInfo.getHost());
        config.put("port", containerInfo.getMappedPort());
        config.put("scheme", "http");
        config.put("uris", containerInfo.getConnectionString());
        return config;
    }
}