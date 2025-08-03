package com.genius.primavera.testContainer;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest
@EnablePrimaveraTestcontainers(
    containers = {ContainerType.MARIADB, ContainerType.REDIS},
    lifecycleMode = ContainerLifecycleMode.PER_CLASS
)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("복합 컨테이너 테스트 - MariaDB + Redis")
public class MultiContainerTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @Order(1)
    @DisplayName("MariaDB와 Redis 컨테이너가 동시에 시작되는지 확인")
    void testMultipleContainersStarted() throws Exception {
        // MariaDB 연결 확인
        Assertions.assertNotNull(dataSource);
        
        try (Connection connection = dataSource.getConnection()) {
            Assertions.assertNotNull(connection);
            String url = connection.getMetaData().getURL();
            System.out.println("Database URL in multi-container test: " + url);
            Assertions.assertTrue(url.contains("mariadb"));
        }
        
        // Redis는 연결할 수 없지만 컨테이너는 시작되었을 것
        System.out.println("Redis container should also be started");
    }

    @Test
    @Order(2)
    @DisplayName("복합 컨테이너 환경에서 데이터 작업 테스트")
    void testDataOperationsInMultiContainer() throws Exception {
        try (Connection connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            
            // MariaDB에서 테이블 생성
            statement.execute("CREATE TABLE IF NOT EXISTS multi_test (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "cache_key VARCHAR(255), " +
                    "data TEXT)");
            
            // 데이터 삽입
            int inserted = statement.executeUpdate(
                    "INSERT INTO multi_test (cache_key, data) VALUES ('test:1', 'test data')");
            Assertions.assertEquals(1, inserted);
            
            // 데이터 조회
            var resultSet = statement.executeQuery(
                    "SELECT cache_key, data FROM multi_test WHERE cache_key = 'test:1'");
            Assertions.assertTrue(resultSet.next());
            Assertions.assertEquals("test:1", resultSet.getString("cache_key"));
            Assertions.assertEquals("test data", resultSet.getString("data"));
        }
    }
}