package com.genius.primavera.dataSource;

import com.genius.primavera.domain.User;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.Mockito.*;

@Slf4j
@JdbcTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Testcontainers
public class SpringJdbcTest {

    @Container
    private static final MariaDBContainer<?> mysqlContainer = new MariaDBContainer<>("mariadb:11.4.7")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/schema.sql");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
        log.info("MySQL 컨테이너 JDBC URL: {}", mysqlContainer.getJdbcUrl());
        System.out.println("MySQL 컨테이너 포트: " + mysqlContainer.getFirstMappedPort());
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("Docker MySQL - jdbcTemplate 테스트")
    public void jdbcTemplateTest() {
        String select = jdbcTemplate.queryForObject("SELECT 'GENIUS' AS ID", String.class);
        Assertions.assertEquals("GENIUS", select);
        System.out.println("jdbcTemplate 테스트 성공: " + select);
    }

    @Test
    @Order(2)
    @DisplayName("Docker MySQL - jdbcTemplate RowMapper 테스트")
    public void jdbcTemplateRowMapperTest() {
        User user = jdbcTemplate.queryForObject("SELECT '1' AS ID, 'genius' AS EMAIL",
                (rs, rowNum) -> User.builder().id(rs.getLong("ID")).email(rs.getString("EMAIL")).build());
        Assertions.assertEquals("genius", user.getEmail());
        System.out.println("jdbcTemplate RowMapper 테스트 성공: " + user.getEmail());
    }

    @Test
    @DisplayName("Docker MySQL - 테이블 쿼리 테스트")
    public void jdbcTemplateTableQueryTest() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS;", Integer.class);
        Assertions.assertEquals(4, count);
        User user = jdbcTemplate.queryForObject("SELECT ID, NICKNAME FROM USERS WHERE ID = 1;",
                (rs, rowNum) -> User.builder().id(rs.getLong("ID")).nickname(rs.getString("NICKNAME")).build());
        Assertions.assertEquals("Genius", user.getNickname());
        System.out.println("테이블 쿼리 테스트 성공: id=" + user.getId() + ", nickname=" + user.getNickname());
    }

    @Test
    @DisplayName("jdbcTemplate 모킹 테스트")
    public void jdbcTemplateMockTest() {
        JdbcTemplate mockJdbcTemplate = mock(JdbcTemplate.class);
        when(mockJdbcTemplate.queryForObject("SELECT 'GENIUS' AS ID", String.class)).thenReturn("GENIUS");
        String result = mockJdbcTemplate.queryForObject("SELECT 'GENIUS' AS ID", String.class);
        Assertions.assertEquals("GENIUS", result);
        verify(mockJdbcTemplate).queryForObject("SELECT 'GENIUS' AS ID", String.class);
    }

    @Test
    @DisplayName("jdbcTemplate RowMapper 모킹 테스트")
    public void jdbcTemplateRowMapperMockTest() {
        JdbcTemplate mockJdbcTemplate = mock(JdbcTemplate.class);
        User expectedUser = User.builder().id(1L).email("genius").build();
        when(mockJdbcTemplate.queryForObject(eq("SELECT '1' AS ID, 'genius' AS EMAIL"), any(RowMapper.class))).thenReturn(expectedUser);
        User user = mockJdbcTemplate.queryForObject("SELECT '1' AS ID, 'genius' AS EMAIL",
                (rs, rowNum) -> User.builder().id(rs.getLong("ID")).email(rs.getString("EMAIL")).build());
        Assertions.assertEquals("genius", user.getEmail());
        verify(mockJdbcTemplate).queryForObject(eq("SELECT '1' AS ID, 'genius' AS EMAIL"), any(RowMapper.class));
    }
}