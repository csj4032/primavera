package com.genius.primavera;

import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int saveUser(String email, String password, String nickname, String role, LocalDateTime createdAt) {
        return jdbcTemplate.update("INSERT INTO users (email, password, nickname, role, created_at) VALUES (?, ?, ?, ?, ?)",
                email, password, nickname, role, createdAt);
    }

    public List<String> getUsers() {
        return jdbcTemplate.query("SELECT password FROM users", (rs) -> {
            var result = new ArrayList<String>();
            while (rs.next()) {
                result.add(rs.getString("password"));
            }
            return result;
        });
    }

    public int deleteAll() {
        return jdbcTemplate.update("DELETE FROM users");
    }
}