package com.genius.primavera.testContainer;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
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
@EnablePrimaveraTestcontainers({ContainerType.MARIADB, ContainerType.REDIS})
@DisplayName("다중 컨테이너 통합 테스트")
class MultiContainerIntegrationTest {

    @Autowired
    private Environment environment;

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    @DisplayName("MariaDB와 Redis 컨테이너가 모두 시작되는지 확인")
    void shouldStartBothMariaDBAndRedisContainers() {
        // MariaDB 컨테이너 확인
        GenericContainer<?> mariadbContainer = PrimaveraTestcontainersContextInitializer.getContainer(ContainerType.MARIADB);
        assertNotNull(mariadbContainer, "MariaDB container should be available");
        assertTrue(mariadbContainer.isRunning(), "MariaDB container should be running");

        // Redis 컨테이너 확인
        GenericContainer<?> redisContainer = PrimaveraTestcontainersContextInitializer.getContainer(ContainerType.REDIS);
        assertNotNull(redisContainer, "Redis container should be available");
        assertTrue(redisContainer.isRunning(), "Redis container should be running");

        log.info("Both MariaDB and Redis containers are running");
        log.info("MariaDB on port: {}", mariadbContainer.getMappedPort(3306));
        log.info("Redis on port: {}", redisContainer.getMappedPort(6379));
    }

    @Test
    @DisplayName("MariaDB와 Redis 모두에 연결 가능한지 확인")
    void shouldConnectToBothDatabases() {
        // MariaDB 연결 테스트
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection()) {
                assertTrue(connection.isValid(5), "MariaDB connection should be valid");

                DatabaseMetaData metaData = connection.getMetaData();
                log.info("Connected to MariaDB: {}", metaData.getDatabaseProductName());
            } catch (SQLException e) {
                fail("Should be able to connect to MariaDB", e);
            }
        }

        // Redis 연결 테스트
        if (redisConnectionFactory != null) {
            try (RedisConnection connection = redisConnectionFactory.getConnection()) {
                assertNotNull(connection, "Redis connection should not be null");
                assertNotNull(connection.ping(), "Should be able to ping Redis");

                log.info("Connected to Redis successfully");
            }
        }

        log.info("Successfully connected to both MariaDB and Redis");
    }

    @Test
    @DisplayName("양쪽 데이터베이스에서 동시 작업 수행")
    void shouldPerformOperationsOnBothDatabases() throws SQLException {
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection()) {
                connection.createStatement().execute(
                        "CREATE TABLE IF NOT EXISTS multi_test (" +
                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                "name VARCHAR(50) NOT NULL, " +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                ")"
                );

                connection.createStatement().execute("INSERT INTO multi_test (name) VALUES ('Multi Container Test')");
                var resultSet = connection.createStatement().executeQuery("SELECT COUNT(*) as count FROM multi_test");
                assertTrue(resultSet.next());
                int count = resultSet.getInt("count");
                assertTrue(count > 0, "Should have data in MariaDB");
                log.info("MariaDB operation successful: {} records", count);
                connection.createStatement().execute("DROP TABLE multi_test");
            }
        }

        if (redisTemplate != null) {
            String sessionKey = "session:user:123";
            String sessionData = "{ \"userId\": 123, \"username\": \"testuser\", \"loginTime\": \"2024-01-01T10:00:00Z\" }";
            redisTemplate.opsForValue().set(sessionKey, sessionData);
            redisTemplate.expire(sessionKey, java.time.Duration.ofSeconds(30));
            Object retrievedSession = redisTemplate.opsForValue().get(sessionKey);
            assertEquals(sessionData, retrievedSession, "Session data should match");
            Long ttl = redisTemplate.getExpire(sessionKey);
            assertTrue(ttl != null && ttl > 0, "TTL should be set");
            log.info("Redis operation successful: session stored with TTL {} seconds", ttl);
            redisTemplate.delete(sessionKey);
        }

        log.info("Successfully performed operations on both databases");
    }

    @Test
    @DisplayName("캐시-데이터베이스 패턴 시뮬레이션")
    void shouldSimulateCacheDatabasePattern() throws SQLException {
        if (dataSource == null || redisTemplate == null) {
            log.warn("Both DataSource and RedisTemplate are required for this test");
            return;
        }

        String userId = "user:1001";
        String userCacheKey = "cache:" + userId;

        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id INT PRIMARY KEY, " +
                            "username VARCHAR(50), " +
                            "email VARCHAR(100)" +
                            ")"
            );
            connection.createStatement().execute(
                    "INSERT INTO users (id, username, email) VALUES " +
                            "(1001, 'testuser', 'test@example.com') " +
                            "ON DUPLICATE KEY UPDATE username=VALUES(username)"
            );
            log.info("User data stored in MariaDB");
        }

        Object cachedUser = redisTemplate.opsForValue().get(userCacheKey);
        assertNull(cachedUser, "Cache should be empty initially");

        String userData = null;
        try (Connection connection = dataSource.getConnection()) {
            var resultSet = connection.createStatement().executeQuery(
                    "SELECT username, email FROM users WHERE id = 1001"
            );

            if (resultSet.next()) {
                userData = String.format("{\"username\":\"%s\",\"email\":\"%s\"}",
                        resultSet.getString("username"),
                        resultSet.getString("email"));
            }
        }

        assertNotNull(userData, "User data should be found in database");
        log.info("User data retrieved from MariaDB: {}", userData);

        redisTemplate.opsForValue().set(userCacheKey, userData, java.time.Duration.ofMinutes(5));

        Object cachedUserAfterStore = redisTemplate.opsForValue().get(userCacheKey);
        assertEquals(userData, cachedUserAfterStore, "Cached data should match database data");

        log.info("Cache-Database pattern simulation successful");

        redisTemplate.delete(userCacheKey);
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("DROP TABLE users");
        }
    }

    @Test
    @DisplayName("환경 프로퍼티에 양쪽 데이터베이스 설정이 모두 존재하는지 확인")
    void shouldHaveBothDatabasePropertiesInEnvironment() {
        String jdbcUrl = environment.getProperty("spring.datasource.url");
        String dbUsername = environment.getProperty("spring.datasource.username");
        String driverClassName = environment.getProperty("spring.datasource.driver-class-name");

        if (jdbcUrl != null) {
            assertTrue(jdbcUrl.startsWith("jdbc:mariadb://"), "Should have MariaDB JDBC URL");
            assertNotNull(dbUsername, "Database username should be set");
            assertEquals("org.mariadb.jdbc.Driver", driverClassName, "Should use MariaDB driver");

            log.info("MariaDB properties configured: {}", jdbcUrl);
        }

        String redisHost = environment.getProperty("spring.data.redis.host");
        String redisPort = environment.getProperty("spring.data.redis.port");

        if (redisHost != null && redisPort != null) {
            assertEquals("localhost", redisHost, "Redis host should be localhost");
            assertTrue(Integer.parseInt(redisPort) > 0, "Redis port should be positive");

            log.info("Redis properties configured: {}:{}", redisHost, redisPort);
        }

        log.info("Environment contains configuration for both databases");
    }
}