package com.genius.primavera.domain.repository;

import com.genius.primavera.domain.model.user.*;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.genius.primavera.domain.model.user.ProviderType.GOOGLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @Commit
    @Transactional
    public void addUser() {
        var connection = UserConnection.builder()
                .email("csj4032@gmail.com")
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
                .email("csj4032@gmail.com")
                .password(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("1234"))
                .nickname("Genius")
                .status(UserStatus.ON)
                .connection(connection)
                .roles(Set.of(role))
                .build();

        userRepository.save(user);
    }

    @Test
    public void findByEmail() {
        var user = userRepository.findByEmail("csj4032@gmail.com").get();
        assertEquals(user.getNickname(), "Genius");
        var connection = user.getConnection();
        assertEquals(connection.getId(), 1);
        assertEquals(connection.getProvider(), GOOGLE);
        assertFalse(user.getRoles().isEmpty());
    }

    @Test
    public void findByNickname() {
        List<UserDto> users = userRepository.findByNickname("csj4032@gmail.com", UserDto.class);
    }

    @Test
    @Commit
    @Transactional
    public void userStatusUpdate() {
        var user = userRepository.findByEmail("csj4032@gmail.com").get();
        user.setStatus(UserStatus.BLOCK);
        var connection = user.getConnection();
        connection.setAccessToken("2");
        var roles = user.getRoles();
        roles.add(Role.builder().id(2l).type(RoleType.MANAGER).build());
        userRepository.save(user);
        assertEquals(user.getNickname(), "Genius");
        assertEquals(user.getStatus(), UserStatus.BLOCK);
    }
}