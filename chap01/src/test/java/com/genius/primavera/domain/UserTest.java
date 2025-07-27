package com.genius.primavera.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

class UserTest {

    @Test
    @DisplayName("User 객체가 정상적으로 생성되어야 한다")
    void userCreationTest() {
        // given
        long id = 1L;
        String email = "test@example.com";
        String password = "password123";
        String nickname = "tester";
        List<Role> roles = Arrays.asList(new Role(), new Role());
        Instant now = Instant.now();

        // when
        User user = new User(id, email, password, nickname, roles, now, now);

        // then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getRoles()).hasSize(2);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("User Builder를 사용하여 객체를 정상적으로 생성해야 한다")
    void userBuilderTest() {
        // given
        long id = 1L;
        String email = "test@example.com";
        String password = "password123";
        String nickname = "tester";
        List<Role> roles = Arrays.asList(new Role(), new Role());
        Instant now = Instant.now();

        // when
        User user = User.builder()
                .id(id)
                .email(email)
                .password(password)
                .nickname(nickname)
                .roles(roles)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getRoles()).hasSize(2);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("동일한 id와 email을 가진 User 객체는 equals 비교에서 true를 반환해야 한다")
    void userEqualsTest() {
        // given
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

        // when & then
        assertThat(user1).isEqualTo(user2);
    }

    @Test
    @DisplayName("다른 id나 email을 가진 User 객체는 equals 비교에서 false를 반환해야 한다")
    void userNotEqualsTest() {
        // given
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

        // when & then
        assertThat(user1).isNotEqualTo(user2);
        assertThat(user1).isNotEqualTo(user3);
    }

    @Test
    @DisplayName("User 기본 생성자로 생성된 객체의 setter 메서드가 정상적으로 동작해야 한다")
    void userSetterMethodsTest() {
        // given
        User user = new User();
        long id = 100L;
        String email = "setter.test@example.com";
        String password = "newPassword123";
        String nickname = "setterTester";
        List<Role> roles = Arrays.asList(
            new Role(1L, "ADMIN", "Administrator role"),
            new Role(2L, "USER", "User role")
        );
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2023, 12, 31, 15, 30);

        // when
        user.setId(id);
        user.setEmail(email);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setRoles(roles);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);

        // then
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
    @DisplayName("User 단일 id 생성자로 생성된 객체의 setter 메서드가 정상적으로 동작해야 한다")
    void userSingleIdConstructorSetterTest() {
        // given
        User user = new User(1L);
        String email = "id.constructor@example.com";
        String password = "idPassword123";
        String nickname = "idUser";
        Instant now = Instant.now();

        // when
        user.setEmail(email);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("User 기본 생성자로 생성하고 null 값 설정이 정상적으로 동작해야 한다")
    void userSetterWithNullValuesTest() {
        // given
        User user = new User();

        // when
        user.setId(0L);
        user.setEmail(null);
        user.setPassword(null);
        user.setNickname(null);
        user.setRoles(null);
        user.setCreatedAt(null);
        user.setUpdatedAt(null);

        // then
        assertThat(user.getId()).isEqualTo(0L);
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getNickname()).isNull();
        assertThat(user.getRoles()).isNull();
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("User 기본 생성자와 단일 id 생성자가 정상적으로 동작해야 한다")
    void userConstructorTest() {
        // given & when
        User defaultUser = new User();
        User idUser = new User(42L);

        // then
        assertThat(defaultUser.getId()).isEqualTo(0L); // primitive long default value
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
