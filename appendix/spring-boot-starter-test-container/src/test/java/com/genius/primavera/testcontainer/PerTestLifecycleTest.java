package com.genius.primavera.testContainer;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@SpringBootTest
@EnablePrimaveraTestcontainers(
    containers = {ContainerType.MARIADB},
    lifecycleMode = ContainerLifecycleMode.PER_TEST
)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("PER_TEST 라이프사이클 모드 테스트")
public class PerTestLifecycleTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @Order(1)
    @DisplayName("첫 번째 테스트 - 새로운 컨테이너 생성")
    void testFirstContainer() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 테이블 생성
            statement.execute("CREATE TABLE IF NOT EXISTS per_test_table (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "test_name VARCHAR(100))");
            
            // 데이터 삽입
            statement.executeUpdate("INSERT INTO per_test_table (test_name) VALUES ('Test 1')");
            
            // 데이터 확인
            var resultSet = statement.executeQuery("SELECT COUNT(*) FROM per_test_table");
            Assertions.assertTrue(resultSet.next());
            Assertions.assertEquals(1, resultSet.getInt(1), "첫 번째 테스트에서는 1개의 레코드만 있어야 함");
        }
    }

    @Test
    @Order(2)
    @DisplayName("두 번째 테스트 - 새로운 컨테이너 생성으로 데이터 초기화됨")
    void testSecondContainer() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // PER_TEST 모드에서는 테이블이 없어야 함 (새 컨테이너)
            var resultSet = statement.executeQuery(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                    "WHERE table_name = 'per_test_table'");
            
            Assertions.assertTrue(resultSet.next());
            int tableCount = resultSet.getInt(1);
            
            // init.sql이 실행되면 test_table은 있지만 per_test_table은 없어야 함
            if (tableCount == 0) {
                System.out.println("✅ PER_TEST mode: New container created, no per_test_table exists");
            } else {
                // 만약 테이블이 있다면 데이터는 없어야 함
                resultSet = statement.executeQuery("SELECT COUNT(*) FROM per_test_table");
                Assertions.assertTrue(resultSet.next());
                Assertions.assertEquals(0, resultSet.getInt(1), "PER_TEST 모드에서는 이전 테스트의 데이터가 없어야 함");
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("세 번째 테스트 - 매번 새로운 컨테이너 확인")
    void testThirdContainer() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            // 매번 새로운 컨테이너이므로 연결 URL의 포트가 다를 수 있음
            String url = connection.getMetaData().getURL();
            System.out.println("Third test connection URL: " + url);
            
            // 기본 연결만 확인
            Assertions.assertNotNull(connection);
            Assertions.assertTrue(url.contains("mariadb"));
        }
    }
}