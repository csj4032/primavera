package com.genius.primavera;

import com.zaxxer.hikari.HikariDataSource;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        primaveraDao.findByName("a");
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
    private final SqlSessionTemplate sqlSessionTemplate;

    User findById(long id) {
        return jdbcTemplate.queryForObject(
                "SELECT 1 AS id, '홍길동' as Name FROM DUAL WHERE 1 = ?",
                (ResultSet rs, int rowNum) -> new User(rs.getLong(1), rs.getString(2)),
                id);
    }

    User findByName(String name) {
        return sqlSessionTemplate.selectOne(
                "SELECT 1 AS id, '홍길동' as Name FROM DUAL WHERE 'a' = #{name}", name);
    }
}