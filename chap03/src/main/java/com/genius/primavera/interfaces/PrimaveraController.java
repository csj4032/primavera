package com.genius.primavera.interfaces;

import com.genius.primavera.PrimaveraDao;
import com.genius.primavera.domain.User;
import com.zaxxer.hikari.HikariDataSource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrimaveraController {

	private final HikariDataSource dataSource;
	private final PrimaveraDao primaveraDao;

	@GetMapping(value = {"/", "/index"})
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