package com.genius.primavera.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    @DisplayName("Role connection successfully processing test")
    void roleCreationTest() {

        long id = 1L;
        String name = "ADMIN";
        String description = "connection";

        Role role = new Role(id, name, description);

        assertThat(role.getId()).isEqualTo(id);
        assertThat(role.getName()).isEqualTo(name);
        assertThat(role.getDescription()).isEqualTo(description);
    }

    @Test
    @DisplayName("Role Buildershould file should successfully successfully test")
    void roleBuilderTest() {

        long id = 1L;
        String name = "ADMIN";
        String description = "connection";

        Role role = Role.builder()
                .id(id)
                .name(name)
                .description(description)
                .build();

        assertThat(role.getId()).isEqualTo(id);
        assertThat(role.getName()).isEqualTo(name);
        assertThat(role.getDescription()).isEqualTo(description);
    }

    @Test
    @DisplayName("connection idshould nameshould test Role connection equals file trueshould file test")
    void roleEqualsTest() {

        Role role1 = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        Role role2 = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("test")
                .build();

        assertThat(role1).isEqualTo(role2);
    }

    @Test
    @DisplayName("test idshould nameshould test Role connection equals file falseshould file test")
    void roleNotEqualsTest() {

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

        assertThat(role1).isNotEqualTo(role2);
        assertThat(role1).isNotEqualTo(role3);
    }

    @Test
    @DisplayName("Role test successfully should not connection successfully file test")
    void roleDefaultConstructorTest() {

        Role role = new Role();

        assertThat(role.getId()).isEqualTo(0L);
        assertThat(role.getName()).isNull();
        assertThat(role.getDescription()).isNull();
    }
}
