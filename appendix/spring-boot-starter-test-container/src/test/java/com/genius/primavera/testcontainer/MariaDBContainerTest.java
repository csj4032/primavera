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
    containers = {ContainerType.MARIADB},
    lifecycleMode = ContainerLifecycleMode.PER_CLASS
)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MariaDB 컨테이너 단독 테스트")
public class MariaDBContainerTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @Order(1)
    @DisplayName("MariaDB 컨테이너가 정상적으로 시작되었는지 확인")
    void testMariaDBContainerStarted() throws Exception {
        Assertions.assertNotNull(dataSource);
        
        try (Connection connection = dataSource.getConnection()) {
            Assertions.assertNotNull(connection);
            String url = connection.getMetaData().getURL();
            System.out.println("MariaDB Connection URL: " + url);
            Assertions.assertTrue(url.contains("mariadb"));
            Assertions.assertTrue(url.contains("primavera_test"));
        }
    }

    @Test
    @Order(2)
    @DisplayName("MariaDB 초기화 스크립트가 실행되었는지 확인")
    void testMariaDBInitScript() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM test_table")) {
            
            Assertions.assertTrue(resultSet.next());
            int count = resultSet.getInt(1);
            Assertions.assertEquals(2, count, "초기화 스크립트가 실행되어 2개의 레코드가 있어야 합니다");
        }
    }

    @Test
    @Order(3)
    @DisplayName("MariaDB에서 데이터 삽입 및 조회 테스트")
    void testMariaDBDataOperations() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 데이터 삽입
            int inserted = statement.executeUpdate("INSERT INTO test_table (name) VALUES ('New Test Data')");
            Assertions.assertEquals(1, inserted);
            
            // 데이터 조회
            ResultSet resultSet = statement.executeQuery("SELECT name FROM test_table WHERE name = 'New Test Data'");
            Assertions.assertTrue(resultSet.next());
            Assertions.assertEquals("New Test Data", resultSet.getString("name"));
        }
    }

    @Test
    @Order(4)
    @DisplayName("동일한 MariaDB 컨테이너를 재사용하는지 확인")
    void testMariaDBContainerReuse() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM test_table")) {
            
            Assertions.assertTrue(resultSet.next());
            int count = resultSet.getInt(1);
            Assertions.assertEquals(3, count, "이전 테스트에서 삽입한 데이터가 유지되어야 합니다");
        }
    }
}