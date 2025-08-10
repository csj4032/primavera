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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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
	@DisplayName("user registration")
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
	@DisplayName("user translated_text_4 translated_text_3 inquiry test")
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
	@DisplayName("user translated_text_4 translated_text_3 inquiry test [Projections]")
	public void findByNickname() {
		List<UserDto> users = userRepository.findByNickname("Genius", UserDto.class);
		users.stream().forEach(e -> log.info("user : {}", e));
		assertTrue(!users.isEmpty());
	}

	@Test
	@Order(4)
	@Rollback(false)
	@Transactional
	@DisplayName("user information translated_text_2 test")
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
	@DisplayName("user translated_text_2 translated_text_3 Truncate")
	public void cleanUp() {
		entityManager.createNativeQuery("TRUNCATE USER").executeUpdate();
		entityManager.createNativeQuery("TRUNCATE USER_CONNECTION").executeUpdate();
		entityManager.createNativeQuery("TRUNCATE USER_ROLE").executeUpdate();
	}
}