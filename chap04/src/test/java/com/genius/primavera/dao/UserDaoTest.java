package com.genius.primavera.dao;

import com.genius.primavera.domain.User;
import com.genius.primavera.testcontainer.EnablePrimaveraTestcontainers;
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
@EnablePrimaveraTestcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDaoTest {

    @Autowired
    private UserDao userDao;

    @Test
    @Order(1)
    @DisplayName("사용자 저장 테스트")
    public void saveUser() {
        User newUser = User.builder().email("email").password("password").nickname("nickname").createdAt(Instant.now()).updatedAt(Instant.now()).build();
        User savedUSer = userDao.save(newUser);
        log.info("저장된 사용자 ID: {}", savedUSer.getId());
        assertNotNull(savedUSer.getId(), "사용자 ID가 null입니다.");
    }

    @Test
    @Order(2)
    @DisplayName("사용자 모두 조회 테스트")
    public void getAllUser() {
        List<User> users = userDao.getUsers();
        log.info("조회된 사용자 수: {}", users.size());
        assertEquals(users.size(), 5);
    }

    @Test
    @Order(3)
    @DisplayName("사용자 조회 테스트")
    public void findById() {
        Long userId = 1L; // 예시로 사용할 사용자 ID
        Optional<User> optionalUser = userDao.findById(userId);
        assertNotNull(optionalUser.get(), "사용자를 찾을 수 없습니다.");
        assertEquals(userId, optionalUser.get().getId(), "사용자 ID가 일치하지 않습니다.");
    }

    @Test
    @Order(4)
    @DisplayName("사용자 삭제 테스트")
    public void deleteAll() {
        int deletedCount = userDao.deleteAll();
        log.info("삭제된 사용자 수: {}", deletedCount);
        assertEquals(deletedCount, 5, "모든 사용자가 삭제되지 않았습니다.");
    }
}