package com.genius.primavera.testcontainer.isolation;

import com.genius.primavera.testcontainer.ContainerType;
import com.genius.primavera.testcontainer.annotation.TestContainersTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @TestContainersTest 메타 어노테이션 사용 예제
 * webEnvironment 설정을 통한 테스트 격리 시연
 */
@Slf4j
public class TestContainersTestExample {
    
    /**
     * MOCK 환경 테스트 (기본값)
     * 웹 서버를 시작하지 않고 MockMvc로 테스트
     */
    @TestContainersTest
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("MOCK 환경 테스트")
    static class MockEnvironmentTest {
        
        @Autowired
        private ApplicationContext applicationContext;
        
        @Autowired
        private JdbcTemplate jdbcTemplate;
        
        @Test
        @DisplayName("MOCK 환경에서 컨테이너 동작 확인")
        void testMockEnvironment() {
            log.info("ApplicationContext Type: {}", applicationContext.getClass().getSimpleName());
            log.info("ApplicationContext ID: {}", System.identityHashCode(applicationContext));
            
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
            assertThat(count).isGreaterThanOrEqualTo(0);
            
            jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('Mock Test', 'mock@test.com')");
            
            Integer newCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
            assertThat(newCount).isEqualTo(count + 1);
        }
    }
    
    /**
     * RANDOM_PORT 환경 테스트
     * 실제 웹 서버를 랜덤 포트로 시작하여 격리된 환경 제공
     */
    @TestContainersTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("RANDOM_PORT 환경 테스트")
    static class RandomPortEnvironmentTest {
        
        @Autowired
        private ApplicationContext applicationContext;
        
        @Autowired
        private JdbcTemplate jdbcTemplate;
        
        @Test
        @DisplayName("RANDOM_PORT 환경에서 격리 확인")
        void testRandomPortIsolation() {
            log.info("ApplicationContext Type: {}", applicationContext.getClass().getSimpleName());
            log.info("ApplicationContext ID: {}", System.identityHashCode(applicationContext));
            
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
            assertThat(count).isGreaterThanOrEqualTo(0);
            
            jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('Random Port Test', 'random@test.com')");
            
            Integer randomPortCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM test_users WHERE email = 'random@test.com'", 
                Integer.class
            );
            assertThat(randomPortCount).isEqualTo(1);
            
            // Mock 환경의 데이터가 없는지 확인 (격리 확인)
            Integer mockCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM test_users WHERE email = 'mock@test.com'", 
                Integer.class
            );
            assertThat(mockCount).isEqualTo(0);
        }
    }
    
    /**
     * 다중 컨테이너와 커스텀 설정 테스트
     */
    @TestContainersTest(
        containers = {ContainerType.MARIADB, ContainerType.REDIS},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.jpa.show-sql=true",
            "logging.level.org.springframework.test=DEBUG"
        }
    )
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("다중 컨테이너 환경 테스트")
    static class MultiContainerTest {
        
        @Autowired
        private ApplicationContext applicationContext;
        
        @Autowired
        private JdbcTemplate jdbcTemplate;
        
        @Test
        @DisplayName("다중 컨테이너 환경에서 동작 확인")
        void testMultiContainerEnvironment() {
            log.info("ApplicationContext Type: {}", applicationContext.getClass().getSimpleName());
            log.info("ApplicationContext ID: {}", System.identityHashCode(applicationContext));
            
            // MariaDB 테스트
            jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('Multi Container', 'multi@test.com')");
            
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM test_users WHERE email = 'multi@test.com'", 
                Integer.class
            );
            assertThat(count).isEqualTo(1);
            
            // Redis bean이 존재하는지 확인 (실제로는 RedisTemplate 등을 주입받아 테스트)
            boolean hasRedisBean = applicationContext.containsBean("redisContainer");
            log.info("Redis Container Bean exists: {}", hasRedisBean);
        }
    }
}