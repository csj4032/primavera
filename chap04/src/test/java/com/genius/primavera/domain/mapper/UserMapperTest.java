package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.mapper.support.UserTableSupport;
import com.genius.primavera.domain.model.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.mybatis.dynamic.sql.SqlBuilder.isIn;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName(value = "유저 관련 테스트")
public class UserMapperTest {

	@Autowired
	private UserMapper userMapper;
	@Autowired
	private UserRoleMapper userRoleMapper;
	private static User source;
	private static List<User> users;
	private static List<User> bulkUsers;

	@BeforeAll
	public static void setUp() {
		users = new ArrayList<>();
		List<Role> roles = List.of(Role.builder().id(1).type(RoleType.USER).build());
		String password = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("secret");
		UserStatus status = UserStatus.ON;
		for (int i = 0; i < 10; i++) {
			users.add(User.builder()
					.email("genius_" + i + "@gmail.com")
					.nickname("genius_" + i)
					.password(password)
					.status(status)
					.roles(roles)
					.regDate(LocalDateTime.now()).modDate(LocalDateTime.now()).build());
		}

		bulkUsers = new ArrayList<>();
		for (int i = 10; i < 100; i++) {
			bulkUsers.add(User.builder()
					.email("genius_" + i + "@gmail.com")
					.nickname("genius_" + i)
					.password(password)
					.status(status)
					.roles(roles)
					.regDate(LocalDateTime.now()).modDate(LocalDateTime.now()).build());
		}

		source = User.builder().email("primavera@gmail.com").nickname("primavera").password(password).status(UserStatus.ON).roles(roles).regDate(LocalDateTime.now()).modDate(LocalDateTime.now()).build();
	}

	@Test
	@Order(0)
	@DisplayName(value = "유저 전체 삭제")
	public void deleteAll() {
		userMapper.deleteAll();
	}

	@Test
	@Order(1)
	@DisplayName(value = "유저 등록")
	public void save() {
		users.forEach(e -> userMapper.save(e));
	}

	@Test
	@Order(2)
	@DisplayName(value = "유저 등록 후 ID 값 반환")
	public void saveSelectKey() {
		userMapper.save(source);
		User destination = userMapper.findById(source.getId());
		Assertions.assertEquals(source.getEmail(), destination.getEmail());
	}

	@Test
	@Order(3)
	@DisplayName(value = "특정 아이디 유저 검색")
	public void findById() {
		User user = userMapper.findById(source.getId());
		Assertions.assertEquals(source.getId(), user.getId());
	}

	@Test
	@Order(4)
	@DisplayName(value = "특정 아이디 유저 수정")
	public void update() {
		source.setNickname("spring");
		source.setModDate(LocalDateTime.now());
		userMapper.update(source);
		User destination = userMapper.findById(source.getId());
		Assertions.assertEquals(destination.getNickname(), source.getNickname());
		Assertions.assertEquals(destination.getId(), source.getId());
	}

	@Test
	@Order(5)
	@DisplayName(value = "특정 아이디 유저 삭제")
	public void deleteById() {
		int count = userMapper.deleteById(source.getId());
		Assertions.assertEquals(1, count);
	}

	@Test
	@Order(6)
	@DisplayName(value = "특정 유저 권한 저장")
	public void saveRoles() {
		for (User user : users)
			userRoleMapper.save(new UserRole(user.getId(), 1));
	}

	@Test
	@Order(7)
	@DisplayName(value = "모든 유저 권한 포함 검색")
	public void findAllWithRoles() {
		List<User> destination = userMapper.findAll();
		Assertions.assertEquals(users, destination);
	}

	@Test
	@Order(8)
	@DisplayName(value = "검색 조건에 따른 결과 반환")
	public void findUserByRequestUser() {
		SelectStatementProvider selectStatement =
				select(UserTableSupport.id, UserTableSupport.email, UserTableSupport.password, UserTableSupport.nickname, UserTableSupport.status, UserTableSupport.regDate, UserTableSupport.modDate)
				.from(UserTableSupport.userTable)
				.where(UserTableSupport.id, isIn(users.stream().map(User::getId).collect(toList())))
				.build()
				.render(RenderingStrategy.MYBATIS3);
		List<User> destination = userMapper.findByRequestUser(selectStatement);
		Assertions.assertEquals(users, destination);
		log.info("{}", destination);
	}

	@Test
	@Order(9)
	@DisplayName(value = "유저 벌크 등록")
	public void bulkSave() {
		userMapper.saveAll(bulkUsers);
		bulkUsers.stream().forEach(System.out::println);
	}
}