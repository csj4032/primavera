package com.genius.primavera.domain.repository;

import com.genius.primavera.domain.model.user.User;
import com.genius.primavera.domain.model.user.UserConnection;
import com.genius.primavera.domain.model.user.UserDto;
import com.genius.primavera.domain.model.user.UserStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import lombok.extern.slf4j.Slf4j;

import static com.genius.primavera.domain.model.user.ProviderType.GOOGLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepositoryTest {

	private static final String EMAIL = "csj4032@gmail.com";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@PersistenceContext(unitName = "default")
	private EntityManager entityManager;

	@Test
	@Order(1)
	@Rollback(false)
	@Transactional
	@DisplayName("사용자 등록")
	public void addUser() {
		var connection = UserConnection.builder()
				.email(EMAIL)
				.provider(GOOGLE)
				.providerId("1")
				.displayName("Genius")
				.profileUrl("")
				.imageUrl("")
				.accessToken("1")
				.expireTime(0)
				.build();

		var role = roleRepository.findById(1l).get();

		var user = User.builder()
				.email(EMAIL)
				.password(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("1234"))
				.nickname("Genius")
				.status(UserStatus.ON)
				.connection(connection)
				.roles(Set.of(role))
				.build();

		userRepository.save(user);
	}

	@Test
	@Order(2)
	@DisplayName("사용자 이메일을 이용한 조회 테스트")
	public void findByEmail() {
		var user = userRepository.findByEmail(EMAIL).get();
		assertEquals(user.getNickname(), "Genius");
		var connection = user.getConnection();
		assertEquals(connection.getId(), 1);
		assertEquals(connection.getProvider(), GOOGLE);
		assertFalse(user.getRoles().isEmpty());
	}

	@Test
	@Order(3)
	@DisplayName("사용자 닉네임을 이용한 조회 테스트 [Projections]")
	public void findByNickname() {
		List<UserDto> users = userRepository.findByNickname("Genius", UserDto.class);
		users.stream().forEach(e -> log.info("user : {}", e));
		assertTrue(!users.isEmpty());
	}

	@Test
	@Order(4)
	@Rollback(false)
	@Transactional
	@DisplayName("사용자 정보 변경 테스트")
	public void userUpdate() {
		var user = userRepository.findByEmail(EMAIL).get();
		user.setStatus(UserStatus.BLOCK);
		var connection = user.getConnection();
		connection.setAccessToken("2");
		user.getRoles().add(roleRepository.findById(1l).get());
		userRepository.save(user);
		assertEquals(user.getNickname(), "Genius");
		assertEquals(user.getStatus(), UserStatus.BLOCK);
	}

	@Test
	@Order(5)
	@DisplayName("사용자 관련 테이블 Truncate")
	public void cleanUp() {
		entityManager.createNativeQuery("TRUNCATE USER").executeUpdate();
		entityManager.createNativeQuery("TRUNCATE USER_CONNECTION").executeUpdate();
		entityManager.createNativeQuery("TRUNCATE USER_ROLE").executeUpdate();
	}
}