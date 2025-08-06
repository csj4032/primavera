package com.genius.primavera.dao;

import com.genius.primavera.domain.User;
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
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("UserDao 통합 테스트")
public class UserDaoTest {

    @Autowired
    private UserDao userDao;

    @Test
    @Order(1)
    @DisplayName("사용자 저장 테스트")
    public void saveUser() {
        User newUser = User.builder().email("email").password("password").nickname("nickname").createdAt(Instant.now()).updatedAt(Instant.now()).build();
        User savedUSer = userDao.save(newUser);
        assertNotNull(savedUSer, "저장된 사용자가 null입니다.");
    }

    @Test
    @Order(2)
    @DisplayName("사용자 저장 후 ID가 할당되었는지 확인")
    public void saveUserIdCheck() {
        User newUser = User.builder().email("email2").password("password2").nickname("nickname2").createdAt(Instant.now()).updatedAt(Instant.now()).build();
        User savedUser = userDao.save(newUser);
        assertNotNull(savedUser, "저장된 사용자가 null입니다.");
        assertEquals(8, savedUser.getId(), "저장된 사용자 ID가 예상과 일치하지 않습니다.");
    }

    @Test
    @Order(3)
    @DisplayName("사용자 모두 조회 테스트")
    public void getAllUser() {
        List<User> users = userDao.getUsers();
        assertEquals(8, users.size(), "사용자 수가 일치하지 않습니다.");
    }

    @Test
    @Order(4)
    @DisplayName("사용자 조회 테스트")
    public void findById() {
        long userId = 1L;
        Optional<User> optionalUser = userDao.findById(userId);
        Assertions.assertTrue(optionalUser.isPresent());
        User user = optionalUser.get();
        assertEquals(userId, user.getId(), "조회된 사용자 ID가 일치하지 않습니다.");
    }

    @Test
    @Order(5)
    @DisplayName("사용자 삭제 테스트")
    public void deleteAll() {
        int deletedCount = userDao.deleteAll();
        assertEquals(8, deletedCount, "삭제된 사용자 수가 일치하지 않습니다.");
    }
}