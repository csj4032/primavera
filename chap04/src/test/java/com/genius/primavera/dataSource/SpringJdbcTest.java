package com.genius.primavera.dataSource;

import com.genius.primavera.domain.User;
import com.genius.primavera.test.annotation.PrimaveraTestContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;

import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@PrimaveraTestContainer
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName(value = "데이터베이스 접속 - TestContainers 기반")
public class SpringJdbcTest {


    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    @Order(1)
    @DisplayName("Docker MySQL - jdbcTemplate 테스트")
    public void jdbcTemplateTest() throws SQLException {
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
    @Order(3)
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
    @Order(4)
    @DisplayName("jdbcTemplate 모킹 테스트")
    public void jdbcTemplateMockTest() {
        JdbcTemplate mockJdbcTemplate = mock(JdbcTemplate.class);
        when(mockJdbcTemplate.queryForObject("SELECT 'GENIUS' AS ID", String.class)).thenReturn("GENIUS");
        String result = mockJdbcTemplate.queryForObject("SELECT 'GENIUS' AS ID", String.class);
        Assertions.assertEquals("GENIUS", result);
        verify(mockJdbcTemplate).queryForObject("SELECT 'GENIUS' AS ID", String.class);
    }

    @Test
    @Order(5)
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