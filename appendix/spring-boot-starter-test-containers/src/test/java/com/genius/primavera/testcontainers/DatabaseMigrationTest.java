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
@DisplayName("데이터베이스 마이그레이션 및 스키마 테스트")
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
        
        log.info("데이터베이스 마이그레이션 테스트 환경 초기화 완료");
    }

    @Test
    @Order(1)
    @DisplayName("초기 스키마 생성 및 검증")
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
        assertTrue(tables.contains("users"), "users 테이블이 생성되어야 함");
        assertTrue(tables.contains("orders"), "orders 테이블이 생성되어야 함");

        Integer userCount = sourceJdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        Integer orderCount = sourceJdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
        
        assertEquals(3, userCount, "3명의 사용자가 있어야 함");
        assertEquals(3, orderCount, "3개의 주문이 있어야 함");

        log.info("초기 스키마 생성 완료: users={}, orders={}", userCount, orderCount);
    }

    @Test
    @Order(2)
    @DisplayName("데이터베이스 간 데이터 마이그레이션")
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

        assertEquals(2, migratedUsers, "ACTIVE 사용자 2명이 마이그레이션되어야 함");
        assertEquals(3, migratedOrders, "ACTIVE 사용자의 주문 3개가 마이그레이션되어야 함");

        log.info("크로스 데이터베이스 마이그레이션 완료: users={}, orders={}", migratedUsers, migratedOrders);
    }

    @Test
    @Order(3)
    @DisplayName("PostgreSQL로 스키마 변환 마이그레이션")
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

        assertEquals(3, pgUsers, "PostgreSQL에 3명의 사용자가 마이그레이션되어야 함");
        assertEquals(3, pgOrders, "PostgreSQL에 3개의 주문이 마이그레이션되어야 함");

        log.info("PostgreSQL 마이그레이션 완료: users={}, orders={}", pgUsers, pgOrders);
    }

    @Test
    @Order(4)
    @DisplayName("스키마 변경 및 데이터 보존")
    void testSchemaEvolution() {
        sourceJdbc.execute("ALTER TABLE users ADD COLUMN phone VARCHAR(20)");
        sourceJdbc.execute("ALTER TABLE users ADD COLUMN last_login TIMESTAMP NULL");

        sourceJdbc.update("UPDATE users SET phone = '010-1234-5678', last_login = NOW() WHERE username = 'alice'");
        sourceJdbc.update("UPDATE users SET phone = '010-9876-5432' WHERE username = 'bob'");

        String alicePhone = sourceJdbc.queryForObject(
            "SELECT phone FROM users WHERE username = 'alice'", String.class);
        assertEquals("010-1234-5678", alicePhone, "Alice의 전화번호가 설정되어야 함");

        Long usersWithLogin = sourceJdbc.queryForObject(
            "SELECT COUNT(*) FROM users WHERE last_login IS NOT NULL", Long.class);
        assertEquals(1L, usersWithLogin, "로그인 기록이 있는 사용자가 1명이어야 함");

        sourceJdbc.execute("CREATE INDEX idx_users_email ON users(email)");
        sourceJdbc.execute("CREATE INDEX idx_orders_date ON orders(order_date)");

        log.info("스키마 진화 테스트 완료: 새 컬럼 및 인덱스 추가");
    }

    @Test
    @Order(5)
    @DisplayName("복잡한 조인 쿼리를 통한 데이터 무결성 검증")
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

        assertEquals(3, userOrderStats.size(), "3명의 사용자 통계가 있어야 함");
        
        Object[] topUser = userOrderStats.get(0);
        assertEquals("alice", topUser[0], "Alice가 최고 구매자여야 함");

        Object[] inactiveUser = userOrderStats.stream()
            .filter(stats -> "charlie".equals(stats[0]))
            .findFirst()
            .orElse(null);
        
        assertNotNull(inactiveUser, "Charlie 통계가 있어야 함");
        assertEquals(0, inactiveUser[3], "Charlie는 주문이 없어야 함");

        log.info("복잡한 쿼리 데이터 무결성 검증 완료");
    }

    @Test
    @Order(6)
    @DisplayName("트랜잭션 롤백 및 복구 테스트")
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
        }, "외래키 제약 조건 위반으로 트랜잭션이 실패해야 함");

        Integer afterUserCount = sourceJdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        Integer afterOrderCount = sourceJdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);

        assertEquals(beforeUserCount, afterUserCount, "사용자 수가 롤백으로 인해 변경되지 않아야 함");
        assertEquals(beforeOrderCount, afterOrderCount, "주문 수가 롤백으로 인해 변경되지 않아야 함");

        log.info("트랜잭션 롤백 테스트 완료: users={}, orders={}", afterUserCount, afterOrderCount);
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
            log.error("테이블 목록 조회 실패", e);
        }
        return tables;
    }
}