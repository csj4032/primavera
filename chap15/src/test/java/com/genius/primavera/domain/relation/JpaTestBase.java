package com.genius.primavera.domain.relation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA 관계 매핑 테스트를 위한 기본 클래스
 * MySQL 8.4.0 TestContainers를 사용하여 테스트 환경 제공
 */
@Testcontainers
public abstract class JpaTestBase {

    @Container
    protected static final MariaDBContainer<?> mysqlContainer = new MariaDBContainer<>("mariadb:11.4.7")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/init.sql");

    protected static EntityManagerFactory entityManagerFactory;
    protected static EntityManager entityManager;
    protected static EntityTransaction entityTransaction;

    @BeforeAll
    public static void setUp() {
        if (!mysqlContainer.isRunning()) {
            mysqlContainer.start();
        }
        
        // 컸테이너 시작 대기
        while (!mysqlContainer.isRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for container", e);
            }
        }
        
        Map<String, String> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver");
        properties.put("jakarta.persistence.jdbc.url", mysqlContainer.getJdbcUrl());
        properties.put("jakarta.persistence.jdbc.user", mysqlContainer.getUsername());
        properties.put("jakarta.persistence.jdbc.password", mysqlContainer.getPassword());
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.use_sql_comments", "true");
        
        entityManagerFactory = Persistence.createEntityManagerFactory("advance", properties);
        entityManager = entityManagerFactory.createEntityManager();
        entityTransaction = entityManager.getTransaction();
    }

    @AfterAll
    public static void tearDown() {
        if (entityManager != null) {
            entityManager.close();
        }
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
        if (mysqlContainer != null) {
            mysqlContainer.stop();
        }
    }
}