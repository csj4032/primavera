package com.genius.primavera;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
@DisplayName(value = "유저 등록, 전체 조회, 삭제")
public class UserDaoTest {

	@Autowired
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	private UserDao userDao;

	@BeforeEach
	public void init() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		userDao = new UserDao(jdbcTemplate);
	}

	@Test
	@Order(1)
	@DisplayName("유저 등록")
	public void saveUser() {
		userDao.saveUser("Genius");
	}

	@Test
	@Order(2)
	@DisplayName("유저 전체 조회")
	public void getUsers() {
		Assertions.assertEquals(List.of("Genius"), userDao.getUsers());
	}

	@Test
	@Order(3)
	@DisplayName("유저 삭제 😱")
	public void deleteAll() {
		userDao.deleteAll();
		Assertions.assertEquals(0, userDao.getUsers().size());
	}
}