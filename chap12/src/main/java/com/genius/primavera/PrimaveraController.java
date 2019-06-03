package com.genius.primavera;

import com.zaxxer.hikari.HikariDataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.sql.DataSource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrimaveraController {

    private final HikariDataSource dataSource;
    private final PrimaveraDao primaveraDao;

    @GetMapping(value = "index")
    public String index() throws SQLException {
        Connection connection = dataSource.getConnection();
        return connection.getCatalog();
    }

    @GetMapping(value = "users/{id}")
    public User user(@PathVariable(value = "id") long id) {
        return primaveraDao.findById(id);
    }
}

@Getter
@Setter
@ToString
@AllArgsConstructor
class User {
    long id;
    String name;
}

@Service
@RequiredArgsConstructor
class PrimaveraDao {
    private final JdbcTemplate jdbcTemplate;

    User findById(long id) {
        return jdbcTemplate.queryForObject(
                "SELECT 1 AS id, '홍길동' as Name FROM DUAL WHERE 1 = ?",
                (ResultSet rs, int rowNum) -> new User(rs.getLong(1), rs.getString(2)),
                id);
    }
}