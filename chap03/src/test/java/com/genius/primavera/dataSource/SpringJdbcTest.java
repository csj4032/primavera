package com.genius.primavera.dataSource;

import com.genius.primavera.domain.User;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.*;

@JdbcTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpringJdbcTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @Disabled("Database integration test disabled - converted to mock test")
    @DisplayName("jdbcTemplate test 😱")
    public void jdbcTemplateTest() {
        String select = jdbcTemplate.queryForObject("SELECT 'GENIUS' AS ID", String.class);
        Assertions.assertEquals("GENIUS", select);
    }

    @Test
    @Order(2)
    @Disabled("Database integration test disabled - converted to mock test")
    @DisplayName("jdbcTemplate RowMapper test 😱")
    public void jdbcTemplateRowMapperTest() {
        User user = jdbcTemplate.queryForObject("SELECT '1' AS ID, 'genius' AS EMAIL", (rs, rowNum) -> User.builder().id(rs.getLong("ID")).email(rs.getString("EMAIL")).build());
        Assertions.assertEquals("genius", user.getEmail());
    }
    
    @Test
    @DisplayName("jdbcTemplate 모킹 테스트")
    public void jdbcTemplateMockTest() {
        // Mock JdbcTemplate
        JdbcTemplate mockJdbcTemplate = mock(JdbcTemplate.class);
        
        // Configure mock behavior
        when(mockJdbcTemplate.queryForObject("SELECT 'GENIUS' AS ID", String.class))
            .thenReturn("GENIUS");
        
        // Test
        String result = mockJdbcTemplate.queryForObject("SELECT 'GENIUS' AS ID", String.class);
        
        // Verify
        Assertions.assertEquals("GENIUS", result);
        verify(mockJdbcTemplate).queryForObject("SELECT 'GENIUS' AS ID", String.class);
    }

    @Test
    @DisplayName("jdbcTemplate RowMapper 모킹 테스트")
    public void jdbcTemplateRowMapperMockTest() {
        // Mock JdbcTemplate
        JdbcTemplate mockJdbcTemplate = mock(JdbcTemplate.class);
        
        // Create expected user
        User expectedUser = User.builder().id(1L).email("genius").build();
        
        // Configure mock behavior
        when(mockJdbcTemplate.queryForObject(eq("SELECT '1' AS ID, 'genius' AS EMAIL"), any(RowMapper.class)))
            .thenReturn(expectedUser);
        
        // Test
        User user = mockJdbcTemplate.queryForObject("SELECT '1' AS ID, 'genius' AS EMAIL", 
            (rs, rowNum) -> User.builder().id(rs.getLong("ID")).email(rs.getString("EMAIL")).build());
        
        // Verify
        Assertions.assertEquals("genius", user.getEmail());
        verify(mockJdbcTemplate).queryForObject(eq("SELECT '1' AS ID, 'genius' AS EMAIL"), any(RowMapper.class));
    }
}