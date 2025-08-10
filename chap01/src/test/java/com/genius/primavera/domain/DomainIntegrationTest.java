package com.genius.primavera.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

class DomainIntegrationTest {

    @Test
    @DisplayName("User, Role, Plant translated_text_2 translated_text_2 translated_text_5 successfully translated_text_5 translated_text_2")
    void domainIntegrationTest() {

        Role adminRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("translated_text_3 translated_text_3")
                .build();

        Role managerRole = Role.builder()
                .id(2L)
                .name("PLANT_MANAGER")
                .description("translated_text_2 translated_text_3")
                .build();

        User manager = User.builder()
                .id(101L)
                .email("manager@example.com")
                .password("securePassword123")
                .nickname("translated_text_2")
                .roles(Arrays.asList(adminRole, managerRole))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Plant plant = Plant.builder()
                .id(1L)
                .name("translated_text_2 translated_text_2")
                .location("translated_text_2 translated_text_3")
                .manager(manager)
                .establishedDate(LocalDateTime.of(2020, 1, 1, 0, 0).toInstant(ZoneOffset.UTC))
                .build();

        assertThat(plant.getManager()).isEqualTo(manager);
        assertThat(plant.getManager().getEmail()).isEqualTo("manager@example.com");

        List<Role> managerRoles = manager.getRoles();
        assertThat(managerRoles).hasSize(2);
        assertThat(managerRoles).contains(adminRole, managerRole);

        boolean hasManagerRole = manager.getRoles().stream().anyMatch(role -> role.getName().equals("PLANT_MANAGER"));
        assertThat(hasManagerRole).isTrue();
    }

    @Test
    @DisplayName("translated_text_2 translated_text_2 translated_text_4 translated_text_3 translated_text_2 test")
    void multiPlantManagerTest() {

        Role multiManagerRole = Role.builder()
                .id(3L)
                .name("MULTI_PLANT_MANAGER")
                .description("translated_text_2 translated_text_2 translated_text_3")
                .build();

        User manager = User.builder()
                .id(102L)
                .email("multi.manager@example.com")
                .password("securePassword456")
                .nickname("translated_text_5")
                .roles(Arrays.asList(multiManagerRole))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Plant seoulPlant = Plant.builder()
                .id(1L)
                .name("translated_text_2 translated_text_2")
                .location("translated_text_2 translated_text_3")
                .manager(manager)
                .establishedDate(LocalDateTime.of(2020, 1, 1, 0, 0).toInstant(ZoneOffset.UTC))
                .build();

        Plant busanPlant = Plant.builder()
                .id(2L)
                .name("translated_text_2 translated_text_2")
                .location("translated_text_2 translated_text_4")
                .manager(manager)
                .establishedDate(LocalDateTime.of(2021, 5, 15, 0, 0).toInstant(ZoneOffset.UTC))
                .build();

        assertThat(seoulPlant.getManager()).isEqualTo(busanPlant.getManager());

        assertThat(manager.getRoles()).hasSize(1);
        assertThat(manager.getRoles().get(0).getName()).isEqualTo("MULTI_PLANT_MANAGER");

        assertThat(seoulPlant.getName()).isNotEqualTo(busanPlant.getName());
        assertThat(seoulPlant.getLocation()).isNotEqualTo(busanPlant.getLocation());
        assertThat(seoulPlant.getEstablishedDate()).isBefore(busanPlant.getEstablishedDate());
    }

    @Test
    @DisplayName("translated_text_3 translated_text_2 translated_text_3 toString translated_text_3 test")
    void domainToStringTest() {

        Role role = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("translated_text_3")
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .roles(Arrays.asList(role))
                .build();

        Plant plant = Plant.builder()
                .id(1L)
                .name("Test Plant")
                .manager(user)
                .build();

        assertThat(role.toString()).contains("id=1", "name=ADMIN", "description=translated_text_3");
        assertThat(user.toString()).contains("id=1", "email=test@example.com");
        assertThat(plant.toString()).contains("id=1", "name=Test Plant");

        assertThat(plant.getManager()).isSameAs(user);
        assertThat(user.getRoles().get(0)).isSameAs(role);
    }
}
