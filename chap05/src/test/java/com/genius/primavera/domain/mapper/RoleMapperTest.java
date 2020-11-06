package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName(value = "권한 관련 테스트")
public class RoleMapperTest {

	@Autowired
	private UserMapper userMapper;
	@Autowired
	private UserRoleMapper userRoleMapper;
	private static List<Role> roles;
	private static User user;

	@BeforeAll
	public static void setUp() {
	}

	@Test
	public void save() {
		roles = new ArrayList<>();
		user = userMapper.findAll().get(0);
	}
}