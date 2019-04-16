package com.genius.primavera.dataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.SQLException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SpringDataSourceTest {

	@Autowired
	private DataSource dataSource;

	@Test
	@DisplayName(value = "스프링 빈을 이용한 데이터베이스 접속")
	public void dataSourceTest() throws SQLException {
		try(var connection = dataSource.getConnection()){
			// 왜 hikari 인가?
			Assertions.assertEquals("com.zaxxer.hikari.pool.HikariProxyConnection", connection.getClass().getName());
			Assertions.assertEquals("primavera", connection.getCatalog());
		}
	}
}