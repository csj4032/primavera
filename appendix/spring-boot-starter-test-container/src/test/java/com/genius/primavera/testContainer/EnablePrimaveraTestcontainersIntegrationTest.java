package com.genius.primavera.testContainer;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(classes = TestConfiguration.class)
@ActiveProfiles("test")
@EnablePrimaveraTestcontainers
@DisplayName("@EnablePrimaveraTestcontainers 통합 테스트")
class EnablePrimaveraTestcontainersIntegrationTest {

    @Autowired
    private Environment environment;

    @Autowired(required = false)
    private DataSource dataSource;

    @Test
    @DisplayName("MariaDB 컨테이너가 시작되고 DataSource가 주입되는지 확인")
    void shouldStartMariaDBContainerAndInjectDataSource() {
        assertNotNull(dataSource, "DataSource should be injected");

        // DataSource 연결 테스트
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(5), "Connection should be valid");

            DatabaseMetaData metaData = connection.getMetaData();
            log.info("Database Product: {}", metaData.getDatabaseProductName());
            log.info("Database Version: {}", metaData.getDatabaseProductVersion());
            log.info("Driver Name: {}", metaData.getDriverName());
            log.info("Connection URL: {}", metaData.getURL());

            assertTrue(metaData.getDatabaseProductName().toLowerCase().contains("mariadb"));

        } catch (SQLException e) {
            fail("Should be able to connect to database", e);
        }
    }

    @Test
    @DisplayName("Spring 환경에 TestContainer 프로퍼티가 설정되는지 확인")
    void shouldHaveTestContainerPropertiesInEnvironment() {
        String jdbcUrl = environment.getProperty("spring.datasource.url");
        String username = environment.getProperty("spring.datasource.username");
        String password = environment.getProperty("spring.datasource.password");
        String driverClassName = environment.getProperty("spring.datasource.driver-class-name");
        assertNotNull(jdbcUrl, "JDBC URL should be set");
        assertNotNull(username, "Username should be set");
        assertNotNull(password, "Password should be set");
        assertNotNull(driverClassName, "Driver class name should be set");
        assertTrue(jdbcUrl.startsWith("jdbc:mariadb://"), "JDBC URL should be for MariaDB");
        assertEquals("org.mariadb.jdbc.Driver", driverClassName, "Driver should be MariaDB driver");
        log.info("JDBC URL: {}", jdbcUrl);
        log.info("Username: {}", username);
        log.info("Driver: {}", driverClassName);
    }

    @Test
    @DisplayName("MariaDB 컨테이너가 실행 중인지 확인")
    void shouldHaveRunningMariaDBContainer() {
        GenericContainer<?> container = PrimaveraTestcontainersContextInitializer.getContainer(ContainerType.MARIADB);
        assertNotNull(container, "MariaDB container should be available");
        assertTrue(container.isRunning(), "MariaDB container should be running");
        Integer mappedPort = container.getMappedPort(3306);
        assertNotNull(mappedPort, "Port should be mapped");
        assertTrue(mappedPort > 0, "Mapped port should be positive");
        log.info("MariaDB container is running on port: {}", mappedPort);
        log.info("Container ID: {}", container.getContainerId());
    }

    @Test
    @DisplayName("데이터베이스 기본 작업이 가능한지 확인")
    void shouldPerformBasicDatabaseOperations() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS test_table (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "name VARCHAR(50) NOT NULL" +
                            ")");

            connection.createStatement().execute("INSERT INTO test_table (name) VALUES ('Test Data')");
            var resultSet = connection.createStatement().executeQuery("SELECT COUNT(*) as count FROM test_table");
            assertTrue(resultSet.next(), "Should have results");
            int count = resultSet.getInt("count");
            assertTrue(count > 0, "Should have at least one record");
            log.info("Successfully inserted and queried {} records", count);
            connection.createStatement().execute("DROP TABLE test_table");
        }
    }
}