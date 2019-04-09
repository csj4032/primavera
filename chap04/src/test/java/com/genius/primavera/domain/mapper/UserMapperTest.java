package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Contact;
import com.genius.primavera.domain.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName(value = "유저 관련 테스트")
public class UserMapperTest {

	@Autowired
	private UserMapper userMapper;
	@Autowired
	private ContactMapper contractsMapper;
	private static User source;
	private static List<User> users;
	private static List<Contact> contacts;

	@BeforeAll
	public static void setUp() {
		users = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			users.add(User.builder().name("Genius_" + i).regDate(LocalDateTime.now()).modDate(LocalDateTime.now()).build());
		}
		source = new User(0, "Anonymous", null, LocalDateTime.now(), LocalDateTime.now());
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
		source = User.builder().name("Primavera").regDate(LocalDateTime.now()).modDate(LocalDateTime.now()).build();
		userMapper.save(source);
		User destination = userMapper.findById(source.getId());
		Assertions.assertEquals(source.getName(), destination.getName());
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
		source.setName("Primavera_0");
		source.setModDate(LocalDateTime.now());
		userMapper.update(source);
		User destination = userMapper.findById(source.getId());
		Assertions.assertEquals(destination.getName(), source.getName());
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
	@DisplayName(value = "모든 유저 검색")
	public void findAll() {
		List<User> destination = userMapper.findAll();
		Assertions.assertEquals(users, destination);
	}

	@Test
	@Order(7)
	@DisplayName(value = "특정 유저 주소록 저장")
	public void saveContacts() {
		contacts = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			Contact contact = Contact.builder().userId(users.get(0).getId()).email("genius@gmail.com").regDate(LocalDateTime.now()).build();
			contacts.add(contact);
			contractsMapper.save(contact);
		}
	}

	@Test
	@Order(8)
	@DisplayName(value = "모든 유저 주소록 포함 검색")
	public void findAllWithContacts() {
		User user = userMapper.findByIdWithContacts(users.get(0).getId());
		Assertions.assertEquals(contacts, user.getContacts());
	}
}