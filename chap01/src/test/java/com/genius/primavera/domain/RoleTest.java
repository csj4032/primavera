package com.genius.primavera.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    @DisplayName("Role 객체가 정상적으로 생성되어야 한다")
    void roleCreationTest() {
        // given
        long id = 1L;
        String name = "ADMIN";
        String description = "시스템 관리자";

        // when
        Role role = new Role(id, name, description);

        // then
        assertThat(role.getId()).isEqualTo(id);
        assertThat(role.getName()).isEqualTo(name);
        assertThat(role.getDescription()).isEqualTo(description);
    }

    @Test
    @DisplayName("Role Builder를 사용하여 객체를 정상적으로 생성해야 한다")
    void roleBuilderTest() {
        // given
        long id = 1L;
        String name = "ADMIN";
        String description = "시스템 관리자";

        // when
        Role role = Role.builder()
                .id(id)
                .name(name)
                .description(description)
                .build();

        // then
        assertThat(role.getId()).isEqualTo(id);
        assertThat(role.getName()).isEqualTo(name);
        assertThat(role.getDescription()).isEqualTo(description);
    }

    @Test
    @DisplayName("동일한 id와 name을 가진 Role 객체는 equals 비교에서 true를 반환해야 한다")
    void roleEqualsTest() {
        // given
        Role role1 = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        Role role2 = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("다른 설명")
                .build();

        // when & then
        assertThat(role1).isEqualTo(role2);
    }

    @Test
    @DisplayName("다른 id나 name을 가진 Role 객체는 equals 비교에서 false를 반환해야 한다")
    void roleNotEqualsTest() {
        // given
        Role role1 = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        Role role2 = Role.builder()
                .id(2L)
                .name("ADMIN")
                .build();

        Role role3 = Role.builder()
                .id(1L)
                .name("USER")
                .build();

        // when & then
        assertThat(role1).isNotEqualTo(role2);
        assertThat(role1).isNotEqualTo(role3);
    }

    @Test
    @DisplayName("Role 기본 생성자로 생성된 객체가 정상적으로 동작해야 한다")
    void roleDefaultConstructorTest() {
        // given & when
        Role role = new Role();

        // then
        assertThat(role.getId()).isEqualTo(0L); // primitive long default value
        assertThat(role.getName()).isNull();
        assertThat(role.getDescription()).isNull();
    }
}
