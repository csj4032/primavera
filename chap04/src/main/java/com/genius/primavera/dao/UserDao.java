package com.genius.primavera.dao;

import com.genius.primavera.domain.User;
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

    public List<User> getUsers() {
        return jdbcTemplate.query(
                "SELECT ID, EMAIL, NICKNAME, CREATED_AT, UPDATED_AT FROM USERS",
                (rs, rowNum) -> User.builder()
                        .id(rs.getLong("ID"))
                        .email(rs.getString("EMAIL"))
                        .nickname(rs.getString("NICKNAME"))
                        .createdAt(rs.getTimestamp("CREATED_AT").toInstant())
                        .updatedAt(rs.getTimestamp("UPDATED_AT").toInstant())
                        .build());
    }

    public User findById(long id) {
        String sql = "SELECT ID, EMAIL, NICKNAME, CREATED_AT, UPDATED_AT FROM USERS WHERE ID = ?";
        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                return User.builder()
                        .id(rs.getLong("ID"))
                        .email(rs.getString("EMAIL"))
                        .nickname(rs.getString("NICKNAME"))
                        .createdAt(rs.getTimestamp("CREATED_AT").toInstant())
                        .updatedAt(rs.getTimestamp("UPDATED_AT").toInstant())
                        .build();
            }
            return null;
        }, id);
    }

    public int deleteAll() {
        return jdbcTemplate.update("DELETE FROM USERS");
    }
}