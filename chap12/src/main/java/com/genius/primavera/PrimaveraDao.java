package com.genius.primavera;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrimaveraDao {
    private final JdbcTemplate jdbcTemplate;
    private final SqlSessionTemplate sqlSessionTemplate;

    User findById(long id) {
        return jdbcTemplate.queryForObject("SELECT 1 AS id, '홍길동' as Name FROM DUAL WHERE 1 = ?", (ResultSet rs, int rowNum) -> new User(rs.getLong(1), rs.getString(2)), id);
    }

    User findByName(String name) {
        return sqlSessionTemplate.selectOne("SELECT 1 AS id, '홍길동' as Name FROM DUAL WHERE 'a' = #{name}", name);
    }
}