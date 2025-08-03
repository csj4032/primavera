package com.genius.primavera.testcontainer.v2;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * 다중 Template을 자동으로 설정하는 Configuration 예시
 * 실제 운영에서는 동적 프로퍼티 기반 설정이 필요
 * 
 * 이 클래스는 예시용으로 컴파일 에러를 방지하기 위해 단순화됨
 * 실제 사용 시에는 테스트 클래스에서 @TestConfiguration으로 필요한 Bean만 정의
 */
@Configuration
public class MultiTemplateAutoConfiguration {

    /**
     * JDBC Template 설정 예시
     * 실제로는 테스트에서 직접 설정하는 것을 권장
     */
    @Configuration
    static class MultiJdbcConfiguration {
        
        // 예시용 DB 1번 설정
        @Bean("exampleDb1DataSource")
        public DataSource exampleDb1DataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            // 실제로는 프로퍼티에서 읽어와야 함
            dataSource.setUrl("jdbc:mariadb://localhost:3306/db1");
            dataSource.setUsername("user1");
            dataSource.setPassword("pass1");
            dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
            return dataSource;
        }
        
        @Bean("exampleDb1JdbcTemplate")
        public JdbcTemplate exampleDb1JdbcTemplate(@Qualifier("exampleDb1DataSource") DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
    }
    
    /**
     * Redis 설정은 실제 테스트에서 필요한 경우에만 import하여 사용
     * @ConditionalOnClass를 사용하여 Redis 의존성이 있을 때만 활성화
     */
    @Configuration
    @ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
    static class OptionalRedisConfiguration {
        // Redis 설정은 실제 Redis 의존성이 있을 때만 동작
        // 테스트에서 직접 Bean을 정의하는 것을 권장
    }

    /**
     * MongoDB 설정은 실제 테스트에서 필요한 경우에만 import하여 사용
     * @ConditionalOnClass를 사용하여 MongoDB 의존성이 있을 때만 활성화
     */
    @Configuration
    @ConditionalOnClass(name = "org.springframework.data.mongodb.core.MongoTemplate")
    static class OptionalMongoConfiguration {
        // MongoDB 설정은 실제 MongoDB 의존성이 있을 때만 동작
        // 테스트에서 직접 Bean을 정의하는 것을 권장
    }
}