package com.genius.primavera.dataSource;

import com.genius.primavera.UserDao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;

import javax.sql.DataSource;


@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
public class UserDaoTest {

	@Autowired
	private DataSource dataSource;
	private UserDao userDao;
	private static PasswordEncoder passwordEncoder;

	@BeforeAll
	public static void setUp() {
		passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@BeforeEach
	public void init() {
		userDao = new UserDao(new JdbcTemplate(dataSource));
	}

	@Test
	@Order(1)
	@DisplayName("유저 등록")
	public void saveUser() {
		userDao.saveUser("genius@gmail.com", passwordEncoder.encode("secret"), "genius", "A", LocalDateTime.now());
	}

	@Test
	@Order(2)
	@DisplayName("유저 전체 조회")
	public void getUsers() {
		userDao.getUsers().forEach(password -> Assertions.assertTrue(passwordEncoder.matches("secret", password)));
	}

	@Test
	@Order(3)
	@DisplayName("유저 삭제 😱")
	public void deleteAll() {
		userDao.deleteAll();
		Assertions.assertEquals(0, userDao.getUsers().size());
	}
}