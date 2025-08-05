package com.genius.primavera.testcontainer.v3;

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

/**
 * 컨테이너 타입별 Spring Bean 등록자 (v3)
 */
@Slf4j
@RequiredArgsConstructor
public class BeanRegistrar {
    
    private final ContainerManager containerManager;
    
    /**
     * 컨테이너 정보를 기반으로 Spring Bean 등록
     */
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
    
    /**
     * 컨테이너 타입별 Bean 생성
     */
    private Object createBean(ContainerManager.ContainerInfo containerInfo) {
        ContainerType type = containerInfo.getType();
        
        switch (type) {
            case MARIADB:
            case MYSQL:
            case POSTGRESQL:
                return createDataSource(containerInfo);
                
            case REDIS:
                return createRedisTemplate(containerInfo);
                
            case KAFKA:
                return createKafkaTemplate(containerInfo);
                
            case MONGODB:
                return createMongoConnectionString(containerInfo);
                
            case ELASTICSEARCH:
                return createElasticsearchConfig(containerInfo);
                
            default:
                log.warn("No bean factory configured for container type: {}", type);
                return null;
        }
    }
    
    /**
     * SQL 데이터베이스용 DataSource 생성
     */
    private DataSource createDataSource(ContainerManager.ContainerInfo containerInfo) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(containerInfo.getJdbcUrl());
        config.setDriverClassName(containerInfo.getType().getDriverClassName());
        config.setUsername(containerInfo.getConfig().getUsername());
        config.setPassword(containerInfo.getConfig().getPassword());
        
        // HikariCP 최적화 설정
        config.setPoolName(containerInfo.getName() + "-pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        
        return new HikariDataSource(config);
    }
    
    /**
     * Redis용 RedisTemplate 생성
     */
    private RedisTemplate<String, Object> createRedisTemplate(ContainerManager.ContainerInfo containerInfo) {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(containerInfo.getHost());
        redisConfig.setPort(containerInfo.getMappedPort());
        
        String password = containerInfo.getConfig().getPassword();
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
    
    /**
     * Kafka용 KafkaTemplate 생성
     */
    private KafkaTemplate<String, Object> createKafkaTemplate(ContainerManager.ContainerInfo containerInfo) {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", containerInfo.getHost() + ":" + containerInfo.getMappedPort());
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
    
    /**
     * MongoDB 연결 문자열 생성
     */
    private String createMongoConnectionString(ContainerManager.ContainerInfo containerInfo) {
        return String.format("mongodb://%s:%d/%s", 
            containerInfo.getHost(), containerInfo.getMappedPort(), 
            containerInfo.getConfig().getDatabase());
    }
    
    /**
     * Elasticsearch 설정 생성
     */
    private Map<String, Object> createElasticsearchConfig(ContainerManager.ContainerInfo containerInfo) {
        Map<String, Object> config = new HashMap<>();
        config.put("host", containerInfo.getHost());
        config.put("port", containerInfo.getMappedPort());
        config.put("scheme", "http");
        config.put("uris", "http://" + containerInfo.getHost() + ":" + containerInfo.getMappedPort());
        return config;
    }
}