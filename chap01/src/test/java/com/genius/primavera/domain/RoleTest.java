package com.genius.primavera.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    @DisplayName("Role translated_text_3 successfully translated_text_11 translated_text_2")
    void roleCreationTest() {

        long id = 1L;
        String name = "ADMIN";
        String description = "translated_text_3 translated_text_3";

        Role role = new Role(id, name, description);

        assertThat(role.getId()).isEqualTo(id);
        assertThat(role.getName()).isEqualTo(name);
        assertThat(role.getDescription()).isEqualTo(description);
    }

    @Test
    @DisplayName("Role Buildertranslated_text_1 translated_text_4 translated_text_1 successfully translated_text_10 translated_text_2")
    void roleBuilderTest() {

        long id = 1L;
        String name = "ADMIN";
        String description = "translated_text_3 translated_text_3";

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
    @DisplayName("translated_text_3 idtranslated_text_1 nametranslated_text_1 translated_text_2 Role translated_text_3 equals translated_text_4 truetranslated_text_1 translated_text_4 translated_text_2")
    void roleEqualsTest() {

        Role role1 = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        Role role2 = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("translated_text_2 translated_text_2")
                .build();

        assertThat(role1).isEqualTo(role2);
    }

    @Test
    @DisplayName("translated_text_2 idtranslated_text_1 nametranslated_text_1 translated_text_2 Role translated_text_3 equals translated_text_4 falsetranslated_text_1 translated_text_4 translated_text_2")
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
    @DisplayName("Role translated_text_2 translated_text_10 translated_text_9 translated_text_3 successfully translated_text_4 translated_text_2")
    void roleDefaultConstructorTest() {

        Role role = new Role();

        assertThat(role.getId()).isEqualTo(0L);
        assertThat(role.getName()).isNull();
        assertThat(role.getDescription()).isNull();
    }
}
