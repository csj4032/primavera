package com.genius.primavera.dataSource;

import com.genius.primavera.domain.model.user.User;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@JdbcTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpringJdbcTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("jdbcTemplate test 😱")
    public void jdbcTemplateTest() {
        String select = jdbcTemplate.queryForObject("SELECT 'GENIUS' AS ID", String.class);
        Assertions.assertEquals("GENIUS", select);
    }

    @Test
    @Order(2)
    @DisplayName("jdbcTemplate RowMapper test 😱")
    public void jdbcTemplateRowMapperTest() {
        User user = jdbcTemplate.queryForObject("SELECT '1' AS ID, 'genius' AS EMAIL", (rs, rowNum) -> User.builder().id(rs.getLong("ID")).email(rs.getString("EMAIL")).build());
        Assertions.assertEquals("genius", user.getEmail());
    }
}