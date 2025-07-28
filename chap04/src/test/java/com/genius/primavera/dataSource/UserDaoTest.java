package com.genius.primavera.dataSource;

import com.genius.primavera.dao.UserDao;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.checkerframework.checker.index.qual.SameLen;

import javax.sql.DataSource;
import java.time.Instant;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "spring.cloud.vault.enabled=false")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDaoTest {

    @Container
    private static final MariaDBContainer<?> mysqlContainer = new MariaDBContainer<>("mariadb:11.4.7").withInitScript("sql/schema.sql");

    @DynamicPropertySource
    static void mariadbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
        log.info("MariaDB 컨테이너 JDBC URL: {}", mysqlContainer.getJdbcUrl());
        log.info("MariaDB 컨테이너 포트: {}", mysqlContainer.getFirstMappedPort());
    }

    @Autowired
    private UserDao userDao;
    private static PasswordEncoder passwordEncoder;

    @BeforeAll
    public static void setUp() {
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Test
    @Order(1)
    @DisplayName("유저 등록")
    public void saveUser() {
        int result = userDao.saveUser("mbappé@gmail.com", passwordEncoder.encode("password"), "Mbappé", "A", Instant.now());
        Assertions.assertEquals(1, result);
        log.info("유저가 성공적으로 등록되었습니다.");
    }

    @Test
    @Order(2)
    @DisplayName("유저 전체 조회")
    public void getUsers() {
        var users = userDao.getUsers();
        Assertions.assertFalse(users.isEmpty());
        log.info("유저 조회 및 비밀번호 검증에 성공했습니다.");
    }

    @Test
    @Order(3)
    @DisplayName("유저 삭제 😱")
    public void deleteAll() {
        int deletedCount = userDao.deleteAll();
        Assertions.assertTrue(deletedCount > 0);
        Assertions.assertEquals(0, userDao.getUsers().size());
        log.info("모든 유저가 성공적으로 삭제되었습니다.");
    }
}