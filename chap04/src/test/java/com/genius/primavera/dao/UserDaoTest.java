package com.genius.primavera.dao;

import com.genius.primavera.domain.User;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SpringBootTest
@EnableTestContainers
@ActiveProfiles("test")
@DisplayName("UserDao translated_text_2 test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDaoTest {

    @Autowired
    private UserDao userDao;

    @Test
    @Order(1)
    @DisplayName("user translated_text_2 test")
    public void saveUser() {
        User newUser = User.builder().email("email").password("password").nickname("nickname").createdAt(Instant.now()).updatedAt(Instant.now()).build();
        User savedUSer = userDao.save(newUser);
        assertNotNull(savedUSer, "translated_text_2 usertranslated_text_1 nulltranslated_text_3.");
    }

    @Test
    @Order(2)
    @DisplayName("user translated_text_2 translated_text_1 IDtranslated_text_1 translated_text_6 verification")
    public void saveUserIdCheck() {
        User newUser = User.builder().email("email2").password("password2").nickname("nickname2").createdAt(Instant.now()).updatedAt(Instant.now()).build();
        User savedUser = userDao.save(newUser);
        assertNotNull(savedUser, "translated_text_2 usertranslated_text_1 nulltranslated_text_3.");
        assertEquals(8, savedUser.getId(), "translated_text_2 user IDtranslated_text_1 translated_text_3 translated_text_4 translated_text_4.");
    }

    @Test
    @Order(3)
    @DisplayName("user translated_text_2 inquiry test")
    public void getAllUser() {
        List<User> users = userDao.getUsers();
        assertEquals(8, users.size(), "user translated_text_1 translated_text_4 translated_text_4.");
    }

    @Test
    @Order(4)
    @DisplayName("user inquiry test")
    public void findById() {
        long userId = 1L;
        Optional<User> optionalUser = userDao.findById(userId);
        Assertions.assertTrue(optionalUser.isPresent());
        User user = optionalUser.get();
        assertEquals(userId, user.getId(), "inquiry user IDtranslated_text_1 translated_text_4 translated_text_4.");
    }

    @Test
    @Order(5)
    @DisplayName("user deletion test")
    public void deleteAll() {
        int deletedCount = userDao.deleteAll();
        assertEquals(8, deletedCount, "deletion user translated_text_1 translated_text_4 translated_text_4.");
    }
}