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
@DisplayName(value = "연락 관련 테스트")
public class ContractsMapperTest {

	@Autowired
	private UserMapper userMapper;
	@Autowired
	private ContactMapper contractMapper;
	private static List<Contact> contacts;
	private static User user;

	@BeforeAll
	public static void setUp() {
	}

	@Test
	public void save() {
		contacts = new ArrayList<>();
		user = userMapper.findAll().get(0);
		for (int i = 0; i < 100; i++) {
			Contact contact = Contact.builder().userId(user.getId()).email("genius@gmail.com").regDate(LocalDateTime.now()).build();
			contacts.add(contact);
			contractMapper.save(contact);
		}
	}
}