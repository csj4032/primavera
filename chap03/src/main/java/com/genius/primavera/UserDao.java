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

	public int saveUser(String name) {
		return jdbcTemplate.update("INSERT INTO USER (NAME, REG_DATE, MOD_DATE) VALUES (?, ?, ?)", name, LocalDateTime.now(), LocalDateTime.now());
	}

	public List<String> getUsers() {
		return jdbcTemplate.query("SELECT NAME FROM USER", (rs) -> {
			var result = new ArrayList<String>();
			while (rs.next()) {
				result.add(rs.getString("NAME"));
			}
			return result;
		});
	}

	public int deleteAll() {
		return jdbcTemplate.update("DELETE FROM USER");
	}
}