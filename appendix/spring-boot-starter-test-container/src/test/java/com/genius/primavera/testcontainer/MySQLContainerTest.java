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
@ActiveProfiles("test")
@DisplayName("MySQL 컨테이너 단독 테스트")
@EnablePrimaveraTestcontainers(containers = {ContainerType.MYSQL})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MySQLContainerTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @Order(1)
    @DisplayName("MySQL 컨테이너가 정상적으로 시작되었는지 확인")
    void testMySQLContainerStarted() throws Exception {
        Assertions.assertNotNull(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            Assertions.assertNotNull(connection);
            String url = connection.getMetaData().getURL();
            String productName = connection.getMetaData().getDatabaseProductName();
            System.out.println("MySQL Connection URL: " + url);
            System.out.println("Database Product Name: " + productName);
            Assertions.assertTrue(url.contains("mysql"));
            Assertions.assertTrue(productName.toLowerCase().contains("mysql"));
        }
    }

    @Test
    @Order(2)
    @DisplayName("MySQL에서 테이블 생성 및 데이터 조작 테스트")
    void testMySQLTableOperations() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS mysql_test_table (" + "id INT AUTO_INCREMENT PRIMARY KEY, " + "data VARCHAR(100), " + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            int inserted = statement.executeUpdate("INSERT INTO mysql_test_table (data) VALUES ('MySQL Test Data')");
            Assertions.assertEquals(1, inserted);
            ResultSet resultSet = statement.executeQuery("SELECT data FROM mysql_test_table WHERE data = 'MySQL Test Data'");
            Assertions.assertTrue(resultSet.next());
            Assertions.assertEquals("MySQL Test Data", resultSet.getString("data"));
        }
    }

    @Test
    @Order(3)
    @DisplayName("MySQL 특화 기능 테스트 - JSON 타입")
    void testMySQLJsonFeature() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS json_test (" + "id INT AUTO_INCREMENT PRIMARY KEY, " + "config JSON)");
            statement.executeUpdate("INSERT INTO json_test (config) VALUES ('{\"key\": \"value\", \"number\": 42}')");
            ResultSet resultSet = statement.executeQuery("SELECT JSON_EXTRACT(config, '$.key') as json_value FROM json_test");
            Assertions.assertTrue(resultSet.next());
            String jsonValue = resultSet.getString("json_value");
            Assertions.assertTrue(jsonValue.contains("value"));
        }
    }
}