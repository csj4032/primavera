package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * Chapter 13 - OneToMany 관계 매핑 테스트를 위한 TestContainers 기반 클래스
 * MySQL 컨테이너를 사용하여 oneToMany 패키지 엔티티만 로드합니다.
 */
@Slf4j
@Testcontainers
public abstract class BaseOneToManyJpaTest {

    @Container
    protected static final MariaDBContainer<?> mysqlContainer = new MariaDBContainer<>("mariadb:11.4.7")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/init-db.sql");

    protected static EntityManagerFactory entityManagerFactory;
    protected static EntityManager entityManager;
    protected static EntityTransaction entityTransaction;

    @BeforeAll
    public static void setUpEntityManager() {
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
        properties.put("hibernate.connection.characterEncoding", "utf8");
        properties.put("hibernate.connection.useUnicode", "true");
        properties.put("hibernate.globally_quoted_identifiers", "true");

        log.info("Creating EntityManagerFactory with TestContainers MySQL: {}", mysqlContainer.getJdbcUrl());
        
        entityManagerFactory = Persistence.createEntityManagerFactory("oneToMany", properties);
        entityManager = entityManagerFactory.createEntityManager();
        entityTransaction = entityManager.getTransaction();
    }

    @AfterAll
    public static void tearDownEntityManager() {
        if (entityTransaction != null && entityTransaction.isActive()) {
            entityTransaction.rollback();
        }
        if (entityManager != null) {
            entityManager.close();
        }
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }
}