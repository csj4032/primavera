package com.genius.primavera.dao;

import com.genius.primavera.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("UserDao 통합 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDaoTest {

    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", mariadb::getDriverClassName);
    }

    @Autowired
    private UserDao userDao;

    @Test
    @Order(1)
    @DisplayName("사용자 저장 테스트")
    public void saveUser() {
        User newUser = User.builder().email("email").password("password").nickname("nickname").createdAt(Instant.now()).updatedAt(Instant.now()).build();
        User savedUSer = userDao.save(newUser);
        log.info("저장된 사용자 ID: {}", savedUSer.getId());
        assertNotNull(savedUSer.getId(), "사용자 ID가 null입니다.");
    }

    @Test
    @Order(2)
    @DisplayName("사용자 모두 조회 테스트")
    public void getAllUser() {
        List<User> users = userDao.getUsers();
        log.info("조회된 사용자 수: {}", users.size());
        assertEquals(6, users.size());
    }

    @Test
    @Order(3)
    @DisplayName("사용자 조회 테스트")
    public void findById() {
        Long userId = 1L;
        Optional<User> optionalUser = userDao.findById(userId);
        assertNotNull(optionalUser.get(), "사용자를 찾을 수 없습니다.");
        assertEquals(userId, optionalUser.get().getId(), "사용자 ID가 일치하지 않습니다.");
    }

    @Test
    @Order(4)
    @DisplayName("사용자 삭제 테스트")
    public void deleteAll() {
        int deletedCount = userDao.deleteAll();
        log.info("삭제된 사용자 수: {}", deletedCount);
        assertEquals(deletedCount, 6, "모든 사용자가 삭제되지 않았습니다.");
    }
}