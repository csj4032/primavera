package com.genius.primavera.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

class UserTest {

    @Test
    @DisplayName("User translated_text_3 successfully translated_text_11 translated_text_2")
    void userCreationTest() {

        long id = 1L;
        String email = "test@example.com";
        String password = "password123";
        String nickname = "tester";
        List<Role> roles = Arrays.asList(new Role(), new Role());
        Instant now = Instant.now();

        User user = new User(id, email, password, nickname, roles, now, now);

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getRoles()).hasSize(2);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("User Buildertranslated_text_1 translated_text_4 translated_text_1 successfully translated_text_10 translated_text_2")
    void userBuilderTest() {

        long id = 1L;
        String email = "test@example.com";
        String password = "password123";
        String nickname = "tester";
        List<Role> roles = Arrays.asList(new Role(), new Role());
        Instant now = Instant.now();

        User user = User.builder()
                .id(id)
                .email(email)
                .password(password)
                .nickname(nickname)
                .roles(roles)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getRoles()).hasSize(2);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("translated_text_3 idtranslated_text_1 emailtranslated_text_1 translated_text_2 User translated_text_3 equals translated_text_4 truetranslated_text_1 translated_text_4 translated_text_2")
    void userEqualsTest() {

        User user1 = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("differentPassword")
                .nickname("differentNickname")
                .build();

        assertThat(user1).isEqualTo(user2);
    }

    @Test
    @DisplayName("translated_text_2 idtranslated_text_1 emailtranslated_text_1 translated_text_2 User translated_text_3 equals translated_text_4 falsetranslated_text_1 translated_text_4 translated_text_2")
    void userNotEqualsTest() {

        User user1 = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("test@example.com")
                .build();

        User user3 = User.builder()
                .id(1L)
                .email("different@example.com")
                .build();

        assertThat(user1).isNotEqualTo(user2);
        assertThat(user1).isNotEqualTo(user3);
    }

    @Test
    @DisplayName("User translated_text_2 translated_text_10 translated_text_9 translated_text_3 setter translated_text_4 successfully translated_text_4 translated_text_2")
    void userSetterMethodsTest() {

        User user = new User();
        long id = 100L;
        String email = "setter.test@example.com";
        String password = "newPassword123";
        String nickname = "setterTester";
        List<Role> roles = Arrays.asList(
                new Role(1L, "ADMIN", "Administrator role"),
                new Role(2L, "USER", "User role")
        );
        Instant createdAt = LocalDateTime.of(2023, 1, 1, 10, 0).toInstant(ZoneOffset.UTC);
        Instant updatedAt = LocalDateTime.of(2023, 12, 31, 15, 30).toInstant(ZoneOffset.UTC);

        user.setId(id);
        user.setEmail(email);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setRoles(roles);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getRoles()).hasSize(2);
        assertThat(user.getRoles()).containsExactlyElementsOf(roles);
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("User translated_text_2 id translated_text_10 translated_text_9 translated_text_3 setter translated_text_4 successfully translated_text_4 translated_text_2")
    void userSingleIdConstructorSetterTest() {

        User user = new User(1L);
        String email = "id.constructor@example.com";
        String password = "idPassword123";
        String nickname = "idUser";
        Instant now = Instant.now();

        user.setEmail(email);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("User translated_text_2 translated_text_10 translated_text_10 null translated_text_1 translated_text_3 successfully translated_text_4 translated_text_2")
    void userSetterWithNullValuesTest() {

        User user = new User();

        user.setId(0L);
        user.setEmail(null);
        user.setPassword(null);
        user.setNickname(null);
        user.setRoles(null);
        user.setCreatedAt(null);
        user.setUpdatedAt(null);

        assertThat(user.getId()).isEqualTo(0L);
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getNickname()).isNull();
        assertThat(user.getRoles()).isNull();
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("User translated_text_2 translated_text_1 translated_text_2 id translated_text_10 successfully translated_text_4 translated_text_2")
    void userConstructorTest() {

        User defaultUser = new User();
        User idUser = new User(42L);

        assertThat(defaultUser.getId()).isEqualTo(0L);
        assertThat(defaultUser.getEmail()).isNull();
        assertThat(defaultUser.getPassword()).isNull();
        assertThat(defaultUser.getNickname()).isNull();
        assertThat(defaultUser.getRoles()).isNull();
        assertThat(defaultUser.getCreatedAt()).isNull();
        assertThat(defaultUser.getUpdatedAt()).isNull();

        assertThat(idUser.getId()).isEqualTo(42L);
        assertThat(idUser.getEmail()).isNull();
        assertThat(idUser.getPassword()).isNull();
        assertThat(idUser.getNickname()).isNull();
        assertThat(idUser.getRoles()).isNull();
        assertThat(idUser.getCreatedAt()).isNull();
        assertThat(idUser.getUpdatedAt()).isNull();
    }
}
