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
@DisplayName("UserDao test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDaoTest {

    @Autowired
    private UserDao userDao;

    @Test
    @Order(1)
    @DisplayName("user test")
    public void saveUser() {
        User newUser = User.builder().email("email").password("password").nickname("nickname").createdAt(Instant.now()).updatedAt(Instant.now()).build();
        User savedUSer = userDao.save(newUser);
        assertNotNull(savedUSer, "test usershould nullconnection.");
    }

    @Test
    @Order(2)
    @DisplayName("user test should IDshould return verification")
    public void saveUserIdCheck() {
        User newUser = User.builder().email("email2").password("password2").nickname("nickname2").createdAt(Instant.now()).updatedAt(Instant.now()).build();
        User savedUser = userDao.save(newUser);
        assertNotNull(savedUser, "test usershould nullconnection.");
        assertEquals(8, savedUser.getId(), "test user IDshould connection file.");
    }

    @Test
    @Order(3)
    @DisplayName("user test inquiry test")
    public void getAllUser() {
        List<User> users = userDao.getUsers();
        assertEquals(8, users.size(), "user should file.");
    }

    @Test
    @Order(4)
    @DisplayName("user inquiry test")
    public void findById() {
        long userId = 1L;
        Optional<User> optionalUser = userDao.findById(userId);
        Assertions.assertTrue(optionalUser.isPresent());
        User user = optionalUser.get();
        assertEquals(userId, user.getId(), "inquiry user IDshould file.");
    }

    @Test
    @Order(5)
    @DisplayName("user deletion test")
    public void deleteAll() {
        int deletedCount = userDao.deleteAll();
        assertEquals(8, deletedCount, "deletion user should file.");
    }
}