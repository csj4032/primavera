package com.genius.primavera.dao;

import com.genius.primavera.domain.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> User.builder()
            .id(rs.getLong("ID"))
            .email(rs.getString("EMAIL"))
            .nickname(rs.getString("NICKNAME"))
            .createdAt(rs.getTimestamp("CREATED_AT").toInstant())
            .updatedAt(rs.getTimestamp("UPDATED_AT").toInstant())
            .build();

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("ID");
    }

    public User save(User newUser) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(newUser);
        Number generatedId = simpleJdbcInsert.executeAndReturnKey(params);
        newUser.setId(generatedId.longValue());
        return newUser;
    }

    public List<User> getUsers() {
        String sql = "SELECT ID, EMAIL, NICKNAME, CREATED_AT, UPDATED_AT FROM USERS";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public Optional<User> findById(long id) {
        String sql = "SELECT ID, EMAIL, NICKNAME, CREATED_AT, UPDATED_AT FROM USERS WHERE ID = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int deleteAll() {
        return jdbcTemplate.update("DELETE FROM USERS");
    }
}