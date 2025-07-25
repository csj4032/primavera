package com.genius.primavera.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
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
        LocalDateTime now = LocalDateTime.now();

        // when
        User user = new User(id, email, password, nickname, roles, now, now);

        // then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getRoles()).hasSize(2);
        assertThat(user.getRegDt()).isEqualTo(now);
        assertThat(user.getModDt()).isEqualTo(now);
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
        LocalDateTime now = LocalDateTime.now();

        // when
        User user = User.builder()
                .id(id)
                .email(email)
                .password(password)
                .nickname(nickname)
                .roles(roles)
                .regDt(now)
                .modDt(now)
                .build();

        // then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getRoles()).hasSize(2);
        assertThat(user.getRegDt()).isEqualTo(now);
        assertThat(user.getModDt()).isEqualTo(now);
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
}
