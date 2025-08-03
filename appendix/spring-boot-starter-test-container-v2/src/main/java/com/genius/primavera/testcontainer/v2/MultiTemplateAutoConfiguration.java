package com.genius.primavera.testcontainer.v2;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * 다중 Template을 자동으로 설정하는 Configuration
 * 실제 운영에서는 @Conditional 어노테이션과 프로퍼티 기반 설정이 필요
 */
@Configuration
public class MultiTemplateAutoConfiguration {

    /**
     * Redis Template 설정 예시
     */
    @Configuration
    @ConditionalOnClass({RedisTemplate.class, RedisConnectionFactory.class})
    static class MultiRedisConfiguration {
        
        // Redis 1번 설정
        @Bean("redis1ConnectionFactory")
        public RedisConnectionFactory redis1ConnectionFactory() {
            // 실제로는 프로퍼티에서 host, port 읽어와야 함
            return new LettuceConnectionFactory("localhost", 6379);
        }
        
        @Bean("redis1Template")
        public RedisTemplate<String, Object> redis1Template(@Qualifier("redis1ConnectionFactory") RedisConnectionFactory connectionFactory) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new StringRedisSerializer());
            return template;
        }
        
        @Bean("redis1StringTemplate")
        public StringRedisTemplate redis1StringTemplate(@Qualifier("redis1ConnectionFactory") RedisConnectionFactory connectionFactory) {
            StringRedisTemplate template = new StringRedisTemplate();
            template.setConnectionFactory(connectionFactory);
            return template;
        }
        
        // Redis 2번 설정
        @Bean("redis2ConnectionFactory")
        public RedisConnectionFactory redis2ConnectionFactory() {
            // 실제로는 다른 프로퍼티에서 host, port 읽어와야 함
            return new LettuceConnectionFactory("localhost", 6380);
        }
        
        @Bean("redis2Template")  
        public RedisTemplate<String, Object> redis2Template(@Qualifier("redis2ConnectionFactory") RedisConnectionFactory connectionFactory) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new StringRedisSerializer());
            return template;
        }
        
        @Bean("redis2StringTemplate")
        public StringRedisTemplate redis2StringTemplate(@Qualifier("redis2ConnectionFactory") RedisConnectionFactory connectionFactory) {
            StringRedisTemplate template = new StringRedisTemplate();
            template.setConnectionFactory(connectionFactory);
            return template;
        }
    }

    /**
     * MongoDB Template 설정 예시  
     */
    @Configuration
    @ConditionalOnClass(MongoTemplate.class)
    static class MultiMongoConfiguration {
        
        // 실제 구현에서는 MongoClient 설정이 필요하지만 예시로 생략
        // @Bean("mongo1Template")
        // public MongoTemplate mongo1Template() {
        //     return new MongoTemplate(mongoClient, "database1");
        // }
        
        // @Bean("mongo2Template")
        // public MongoTemplate mongo2Template() {
        //     return new MongoTemplate(mongoClient, "database2");
        // }
    }

    /**
     * JDBC Template 설정 예시
     */
    @Configuration
    static class MultiJdbcConfiguration {
        
        // DB 1번 설정
        @Bean("db1DataSource")
        public DataSource db1DataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            // 실제로는 프로퍼티에서 읽어와야 함
            dataSource.setUrl("jdbc:mariadb://localhost:3306/db1");
            dataSource.setUsername("user1");
            dataSource.setPassword("pass1");
            dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
            return dataSource;
        }
        
        @Bean("db1JdbcTemplate")
        public JdbcTemplate db1JdbcTemplate(@Qualifier("db1DataSource") DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
        
        // DB 2번 설정
        @Bean("db2DataSource")
        public DataSource db2DataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            // 실제로는 프로퍼티에서 읽어와야 함
            dataSource.setUrl("jdbc:mariadb://localhost:3307/db2");
            dataSource.setUsername("user2");
            dataSource.setPassword("pass2");
            dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
            return dataSource;
        }
        
        @Bean("db2JdbcTemplate")
        public JdbcTemplate db2JdbcTemplate(@Qualifier("db2DataSource") DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
    }
    
    /**
     * 프로퍼티 기반 동적 Bean 생성을 위한 설정 클래스들
     */
    @ConfigurationProperties(prefix = "app.containers")
    static class MultiContainerProperties {
        // 실제 구현에서는 여기서 동적으로 프로퍼티를 읽어서 Bean을 생성
    }
}