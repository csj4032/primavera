package com.genius.primavera.dataSource;

import com.genius.primavera.UserDao;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.time.LocalDateTime;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDaoTest {

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.4.0")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/schema.sql");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        System.out.println("MySQL 컨테이너 JDBC URL: " + mysqlContainer.getJdbcUrl());
        System.out.println("MySQL 컨테이너 포트: " + mysqlContainer.getFirstMappedPort());
    }

    @Autowired
    private DataSource dataSource;
    
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
        int result = userDao.saveUser("mbappé@gmail.com", passwordEncoder.encode("password"), "Mbappé", "A", LocalDateTime.now());
        Assertions.assertEquals(1, result);
        System.out.println("유저가 성공적으로 등록되었습니다.");
    }

    @Test
    @Order(2)
    @DisplayName("유저 전체 조회")
    public void getUsers() {
        var passwords = userDao.getUsers();
        Assertions.assertFalse(passwords.isEmpty());
        passwords.forEach(password -> Assertions.assertTrue(passwordEncoder.matches("password", password)));
        System.out.println("유저 조회 및 비밀번호 검증에 성공했습니다.");
    }

    @Test
    @Order(3)
    @DisplayName("유저 삭제 😱")
    public void deleteAll() {
        int deletedCount = userDao.deleteAll();
        Assertions.assertTrue(deletedCount > 0);
        Assertions.assertEquals(0, userDao.getUsers().size());
        System.out.println("모든 유저가 성공적으로 삭제되었습니다.");
    }
}