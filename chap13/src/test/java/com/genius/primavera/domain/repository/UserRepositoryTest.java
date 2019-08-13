package com.genius.primavera.domain.repository;

import com.genius.primavera.domain.model.user.User;
import com.genius.primavera.domain.model.user.UserStatus;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Disabled
    public void addUser() {
        var user = User.builder()
                .email("csj4032@gmail.com")
                .password(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("1234"))
                .nickname("Genius")
                .status(UserStatus.ON)
                .build();
        userRepository.save(user);
    }

    @Test
    public void findById() {
        var user = userRepository.findByEmail("csj4032@gmail.com").get();
        assertEquals(user.getNickname(), "Genius");
    }

    @Test
    public void userStatusUpdate() {
        var user = userRepository.findByEmail("csj4032@gmail.com").get();
        user.setStatus(UserStatus.BLOCK);
        userRepository.saveAndFlush(user);
        assertEquals(user.getNickname(), "Genius");
        assertEquals(user.getStatus(), UserStatus.BLOCK);
    }
}