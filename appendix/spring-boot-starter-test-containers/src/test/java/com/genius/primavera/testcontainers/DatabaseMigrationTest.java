package com.genius.primavera.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("logging with should connection test")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "sourceDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "targetDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.POSTGRESQL, name = "migrationDb")
})
class DatabaseMigrationTest {

    @Autowired
    @Qualifier("sourceDb")
    private DataSource sourceDataSource;

    @Autowired
    @Qualifier("targetDb") 
    private DataSource targetDataSource;

    @Autowired
    @Qualifier("migrationDb")
    private DataSource migrationDataSource;

    private JdbcTemplate sourceJdbc;
    private JdbcTemplate targetJdbc;
    private JdbcTemplate migrationJdbc;

    @BeforeAll
    void setupDatabases() {
        sourceJdbc = new JdbcTemplate(sourceDataSource);
        targetJdbc = new JdbcTemplate(targetDataSource);
        migrationJdbc = new JdbcTemplate(migrationDataSource);
        
        log.info("logging with test connection completed");
    }

    @Test
    @Order(1)
    @DisplayName("test connection creation should validation")
    void testInitialSchemaCreation() {
        sourceJdbc.execute("""
            CREATE TABLE users (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'
            )
        """);

        sourceJdbc.execute("""
            CREATE TABLE orders (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                product_name VARCHAR(200) NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                order_date DATE NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """);

        sourceJdbc.update("""
            INSERT INTO users (username, email, status) VALUES 
            ('alice', 'alice@test.com', 'ACTIVE'),
            ('bob', 'bob@test.com', 'ACTIVE'),
            ('charlie', 'charlie@test.com', 'INACTIVE')
        """);

        sourceJdbc.update("""
            INSERT INTO orders (user_id, product_name, amount, order_date) VALUES 
            (1, 'Laptop', 1200.00, '2024-01-15'),
            (1, 'Mouse', 25.00, '2024-01-16'),
            (2, 'Keyboard', 80.00, '2024-01-17')
        """);

        List<String> tables = getTables(sourceDataSource);
        assertTrue(tables.contains("users"), "users file creation should");
        assertTrue(tables.contains("orders"), "orders file creation should");

        Integer userCount = sourceJdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        Integer orderCount = sourceJdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
        
        assertEquals(3, userCount, "3test Endpoint connection should");
        assertEquals(3, orderCount, "3test connection should");

        log.info("test connection creation completed: users={}, orders={}", userCount, orderCount);
    }

    @Test
    @Order(2)
    @DisplayName("logging should data with")
    void testCrossDatabaseMigration() {
        targetJdbc.execute("""
            CREATE TABLE users (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'
            )
        """);

        targetJdbc.execute("""
            CREATE TABLE orders (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                product_name VARCHAR(200) NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                order_date DATE NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """);

        List<Object[]> activeUsers = sourceJdbc.query(
            "SELECT username, email, status FROM users WHERE status = 'ACTIVE'",
            (rs, rowNum) -> new Object[]{
                rs.getString("username"),
                rs.getString("email"), 
                rs.getString("status")
            });

        for (Object[] user : activeUsers) {
            targetJdbc.update(
                "INSERT INTO users (username, email, status) VALUES (?, ?, ?)",
                user[0], user[1], user[2]);
        }

        List<Object[]> orders = sourceJdbc.query("""
            SELECT o.user_id, o.product_name, o.amount, o.order_date 
            FROM orders o 
            JOIN users u ON o.user_id = u.id 
            WHERE u.status = 'ACTIVE'
        """, (rs, rowNum) -> new Object[]{
            rs.getLong("user_id"),
            rs.getString("product_name"),
            rs.getBigDecimal("amount"),
            rs.getDate("order_date")
        });

        for (Object[] order : orders) {
            targetJdbc.update(
                "INSERT INTO orders (user_id, product_name, amount, order_date) VALUES (?, ?, ?, ?)",
                order[0], order[1], order[2], order[3]);
        }

        Integer migratedUsers = targetJdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        Integer migratedOrders = targetJdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);

        assertEquals(2, migratedUsers, "ACTIVE user 2test with should");
        assertEquals(3, migratedOrders, "ACTIVE usershould test 3test with should");

        log.info("connection logging with completed: users={}, orders={}", migratedUsers, migratedOrders);
    }

    @Test
    @Order(3)
    @DisplayName("PostgreSQLshould connection test with")
    void testPostgreSQLSchemaMigration() {
        migrationJdbc.execute("""
            CREATE TABLE users (
                id BIGSERIAL PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status VARCHAR(10) DEFAULT 'ACTIVE',
                CHECK (status IN ('ACTIVE', 'INACTIVE'))
            )
        """);

        migrationJdbc.execute("""
            CREATE TABLE orders (
                id BIGSERIAL PRIMARY KEY,
                user_id BIGINT NOT NULL,
                product_name VARCHAR(200) NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                order_date DATE NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """);

        List<Object[]> users = sourceJdbc.query(
            "SELECT username, email, status FROM users",
            (rs, rowNum) -> new Object[]{
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("status")
            });

        for (Object[] user : users) {
            migrationJdbc.update(
                "INSERT INTO users (username, email, status) VALUES (?, ?, ?)",
                user[0], user[1], user[2]);
        }

        List<Object[]> orders = sourceJdbc.query("""
            SELECT u.username, o.product_name, o.amount, o.order_date
            FROM orders o 
            JOIN users u ON o.user_id = u.id
        """, (rs, rowNum) -> new Object[]{
            rs.getString("username"),
            rs.getString("product_name"),
            rs.getBigDecimal("amount"),
            rs.getDate("order_date")
        });

        for (Object[] order : orders) {
            Long userId = migrationJdbc.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, order[0]);
            
            migrationJdbc.update(
                "INSERT INTO orders (user_id, product_name, amount, order_date) VALUES (?, ?, ?, ?)",
                userId, order[1], order[2], order[3]);
        }

        Integer pgUsers = migrationJdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        Integer pgOrders = migrationJdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);

        assertEquals(3, pgUsers, "PostgreSQLshould 3test file exists should");
        assertEquals(3, pgOrders, "PostgreSQLshould 3test connection with should");

        log.info("PostgreSQL with completed: users={}, orders={}", pgUsers, pgOrders);
    }

    @Test
    @Order(4)
    @DisplayName("connection test should data test")
    void testSchemaEvolution() {
        sourceJdbc.execute("ALTER TABLE users ADD COLUMN phone VARCHAR(20)");
        sourceJdbc.execute("ALTER TABLE users ADD COLUMN last_login TIMESTAMP NULL");

        sourceJdbc.update("UPDATE users SET phone = '010-1234-5678', last_login = NOW() WHERE username = 'alice'");
        sourceJdbc.update("UPDATE users SET phone = '010-9876-5432' WHERE username = 'bob'");

        String alicePhone = sourceJdbc.queryForObject(
            "SELECT phone FROM users WHERE username = 'alice'", String.class);
        assertEquals("010-1234-5678", alicePhone, "Aliceshould Endpoint Endpoint should");

        Long usersWithLogin = sourceJdbc.queryForObject(
            "SELECT COUNT(*) FROM users WHERE last_login IS NOT NULL", Long.class);
        assertEquals(1L, usersWithLogin, "should connection test Endpoint 1test should");

        sourceJdbc.execute("CREATE INDEX idx_users_email ON users(email)");
        sourceJdbc.execute("CREATE INDEX idx_orders_date ON orders(order_date)");

        log.info("connection test completed: test should connection test");
    }

    @Test
    @Order(5)
    @DisplayName("connection test connection test data connection validation")
    void testComplexQueryDataIntegrity() {
        List<Object[]> userOrderStats = sourceJdbc.query("""
            SELECT 
                u.username,
                u.email,
                u.status,
                COUNT(o.id) as order_count,
                COALESCE(SUM(o.amount), 0) as total_amount,
                MAX(o.order_date) as last_order_date
            FROM users u
            LEFT JOIN orders o ON u.id = o.user_id
            GROUP BY u.id, u.username, u.email, u.status
            ORDER BY total_amount DESC
        """, (rs, rowNum) -> new Object[]{
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("status"),
            rs.getInt("order_count"),
            rs.getBigDecimal("total_amount"),
            rs.getDate("last_order_date")
        });

        assertEquals(3, userOrderStats.size(), "3test user connection should");
        
        Object[] topUser = userOrderStats.get(0);
        assertEquals("alice", topUser[0], "Aliceshould test Endpoint should");

        Object[] inactiveUser = userOrderStats.stream()
            .filter(stats -> "charlie".equals(stats[0]))
            .findFirst()
            .orElse(null);
        
        assertNotNull(inactiveUser, "Charlie connection should");
        assertEquals(0, inactiveUser[3], "Charlieshould connection should");

        log.info("connection test data connection validation completed");
    }

    @Test
    @Order(6)
    @DisplayName("file test should test")
    void testTransactionRollbackAndRecovery() {
        Integer beforeUserCount = sourceJdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        Integer beforeOrderCount = sourceJdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);

        assertThrows(Exception.class, () -> {
            sourceJdbc.execute("START TRANSACTION");
            try {
                sourceJdbc.update("INSERT INTO users (username, email) VALUES ('david', 'david@test.com')");
                
                sourceJdbc.update("INSERT INTO orders (user_id, product_name, amount, order_date) VALUES (999, 'Invalid Order', 100.00, '2024-01-01')");
                
                sourceJdbc.execute("COMMIT");
            } catch (Exception e) {
                sourceJdbc.execute("ROLLBACK");
                throw e;
            }
        }, "connection test should file should not should");

        Integer afterUserCount = sourceJdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        Integer afterOrderCount = sourceJdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);

        assertEquals(beforeUserCount, afterUserCount, "user should testshould test connection should");
        assertEquals(beforeOrderCount, afterOrderCount, "test should testshould test connection should");

        log.info("file test completed: users={}, orders={}", afterUserCount, afterOrderCount);
    }

    private List<String> getTables(DataSource dataSource) {
        List<String> tables = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME").toLowerCase());
                }
            }
        } catch (Exception e) {
            log.error("connection test inquiry failure", e);
        }
        return tables;
    }
}