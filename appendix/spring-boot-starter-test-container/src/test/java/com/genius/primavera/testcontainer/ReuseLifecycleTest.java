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
    lifecycleMode = ContainerLifecycleMode.REUSE
)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("REUSE 라이프사이클 모드 테스트")
public class ReuseLifecycleTest {

    @Autowired
    private DataSource dataSource;

    private static String firstConnectionUrl = null;

    @Test
    @Order(1)
    @DisplayName("첫 번째 테스트 - 컨테이너 생성 및 데이터 저장")
    void testFirstReuse() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 연결 URL 저장
            firstConnectionUrl = connection.getMetaData().getURL();
            System.out.println("First test connection URL: " + firstConnectionUrl);
            
            // 테이블 생성
            statement.execute("CREATE TABLE IF NOT EXISTS reuse_table (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "data VARCHAR(100))");
            
            // 데이터 삽입
            statement.executeUpdate("INSERT INTO reuse_table (data) VALUES ('Reuse Test Data')");
            
            // 데이터 확인
            var resultSet = statement.executeQuery("SELECT COUNT(*) FROM reuse_table");
            Assertions.assertTrue(resultSet.next());
            Assertions.assertEquals(1, resultSet.getInt(1));
        }
    }

    @Test
    @Order(2)
    @DisplayName("두 번째 테스트 - 동일한 컨테이너 재사용 확인")
    void testSecondReuse() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 동일한 연결 URL인지 확인
            String currentUrl = connection.getMetaData().getURL();
            System.out.println("Second test connection URL: " + currentUrl);
            Assertions.assertEquals(firstConnectionUrl, currentUrl, "REUSE 모드에서는 동일한 컨테이너를 사용해야 함");
            
            // 이전 테스트의 데이터가 유지되어 있는지 확인
            var resultSet = statement.executeQuery("SELECT COUNT(*) FROM reuse_table");
            Assertions.assertTrue(resultSet.next());
            Assertions.assertEquals(1, resultSet.getInt(1), "REUSE 모드에서는 이전 데이터가 유지되어야 함");
            
            // 추가 데이터 삽입
            statement.executeUpdate("INSERT INTO reuse_table (data) VALUES ('Second Test Data')");
        }
    }

    @Test
    @Order(3)
    @DisplayName("세 번째 테스트 - 계속 동일한 컨테이너 사용")
    void testThirdReuse() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 여전히 동일한 연결 URL인지 확인
            String currentUrl = connection.getMetaData().getURL();
            Assertions.assertEquals(firstConnectionUrl, currentUrl);
            
            // 모든 이전 테스트의 데이터가 누적되어 있는지 확인
            var resultSet = statement.executeQuery("SELECT COUNT(*) FROM reuse_table");
            Assertions.assertTrue(resultSet.next());
            Assertions.assertEquals(2, resultSet.getInt(1), "REUSE 모드에서는 모든 테스트의 데이터가 누적되어야 함");
        }
    }
}