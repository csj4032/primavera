package com.genius.primavera.testContainer;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@SpringBootTest
@EnablePrimaveraTestcontainers(
    containers = {ContainerType.POSTGRESQL},
    lifecycleMode = ContainerLifecycleMode.PER_CLASS
)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("PostgreSQL 컨테이너 단독 테스트")
public class PostgreSQLContainerTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @Order(1)
    @DisplayName("PostgreSQL 컨테이너가 정상적으로 시작되었는지 확인")
    void testPostgreSQLContainerStarted() throws Exception {
        Assertions.assertNotNull(dataSource);
        
        try (Connection connection = dataSource.getConnection()) {
            Assertions.assertNotNull(connection);
            String url = connection.getMetaData().getURL();
            String productName = connection.getMetaData().getDatabaseProductName();
            System.out.println("PostgreSQL Connection URL: " + url);
            System.out.println("Database Product Name: " + productName);
            Assertions.assertTrue(url.contains("postgresql"));
            Assertions.assertEquals("PostgreSQL", productName);
        }
    }

    @Test
    @Order(2)
    @DisplayName("PostgreSQL에서 테이블 생성 및 데이터 조작 테스트")
    void testPostgreSQLTableOperations() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 테이블 생성
            statement.execute("CREATE TABLE IF NOT EXISTS postgres_test_table (" +
                    "id SERIAL PRIMARY KEY, " +
                    "data VARCHAR(100), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // 데이터 삽입
            int inserted = statement.executeUpdate(
                    "INSERT INTO postgres_test_table (data) VALUES ('PostgreSQL Test Data')");
            Assertions.assertEquals(1, inserted);
            
            // 데이터 조회
            ResultSet resultSet = statement.executeQuery(
                    "SELECT data FROM postgres_test_table WHERE data = 'PostgreSQL Test Data'");
            Assertions.assertTrue(resultSet.next());
            Assertions.assertEquals("PostgreSQL Test Data", resultSet.getString("data"));
        }
    }

    @Test
    @Order(3)
    @DisplayName("PostgreSQL 특화 기능 테스트 - ARRAY 타입")
    void testPostgreSQLArrayFeature() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // ARRAY 컬럼이 있는 테이블 생성
            statement.execute("CREATE TABLE IF NOT EXISTS array_test (" +
                    "id SERIAL PRIMARY KEY, " +
                    "tags TEXT[])");
            
            // ARRAY 데이터 삽입
            statement.executeUpdate(
                    "INSERT INTO array_test (tags) VALUES (ARRAY['tag1', 'tag2', 'tag3'])");
            
            // ARRAY 데이터 조회
            ResultSet resultSet = statement.executeQuery(
                    "SELECT tags[1] as first_tag FROM array_test");
            Assertions.assertTrue(resultSet.next());
            Assertions.assertEquals("tag1", resultSet.getString("first_tag"));
            
            // ARRAY 포함 여부 확인
            resultSet = statement.executeQuery(
                    "SELECT 'tag2' = ANY(tags) as has_tag FROM array_test");
            Assertions.assertTrue(resultSet.next());
            Assertions.assertTrue(resultSet.getBoolean("has_tag"));
        }
    }
}