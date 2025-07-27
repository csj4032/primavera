package com.genius.primavera;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int saveUser(String email, String password, String nickname, String status, Instant createdAt) {
        return jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, STATUS, CREATED_AT) VALUES (?, ?, ?, ?, ?)",
                email, password, nickname, status, createdAt);
    }

    public List<String> getUsers() {
        return jdbcTemplate.query("SELECT password FROM USERS", (rs) -> {
            var result = new ArrayList<String>();
            while (rs.next()) {
                result.add(rs.getString("password"));
            }
            return result;
        });
    }

    public int deleteAll() {
        return jdbcTemplate.update("DELETE FROM USERS");
    }
}