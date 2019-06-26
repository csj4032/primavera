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

	public int saveUser(String email, String password, String nickname, String status, LocalDateTime regDate) {
		return jdbcTemplate.update("INSERT INTO USER (EMAIL, PASSWORD, NICK_NAME, STATUS, REG_DT) VALUES (? ,?, ?, ?, ?)", email, password, nickname, status, regDate);
	}

	public List<String> getUsers() {
		return jdbcTemplate.query("SELECT PASSWORD FROM USER", (rs) -> {
			var result = new ArrayList<String>();
			while (rs.next()) {
				result.add(rs.getString("PASSWORD"));
			}
			return result;
		});
	}

	public int deleteAll() {
		return jdbcTemplate.update("DELETE FROM USER");
	}
}