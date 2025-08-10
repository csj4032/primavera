package com.genius.primavera.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class PlantTest {

    @Test
    @DisplayName("Plant connection successfully processing test")
    void plantCreationTest() {
        long id = 1L;
        String name = "test";
        String location = "test connection";
        User manager = new User(101L, "manager@example.com", "password", "test", null, Instant.now(), Instant.now());
        Instant establishedDate = Instant.parse("2020-01-01T00:00:00Z");
        Plant plant = new Plant(id, name, location, manager, establishedDate);
        assertThat(plant.getId()).isEqualTo(id);
        assertThat(plant.getName()).isEqualTo(name);
        assertThat(plant.getLocation()).isEqualTo(location);
        assertThat(plant.getManager()).isEqualTo(manager);
        assertThat(plant.getEstablishedDate()).isEqualTo(establishedDate);
    }

    @Test
    @DisplayName("Plant Buildershould file should successfully successfully test")
    void plantBuilderTest() {
        long id = 1L;
        String name = "test";
        String location = "test connection";
        User manager = User.builder().id(101L).email("manager@example.com").build();
        Instant establishedDate = Instant.parse("2020-01-01T00:00:00Z");
        Plant plant = Plant.builder()
                .id(id)
                .name(name)
                .location(location)
                .manager(manager)
                .establishedDate(establishedDate)
                .build();
        assertThat(plant.getId()).isEqualTo(id);
        assertThat(plant.getName()).isEqualTo(name);
        assertThat(plant.getLocation()).isEqualTo(location);
        assertThat(plant.getManager().getId()).isEqualTo(manager.getId());
        assertThat(plant.getEstablishedDate()).isEqualTo(establishedDate);
    }

    @Test
    @DisplayName("connection idshould nameshould test Plant connection equals file trueshould file test")
    void plantEqualsTest() {
        Plant plant1 = Plant.builder().id(1L).name("test").build();
        Plant plant2 = Plant.builder().id(1L).name("test").location("test").manager(new User(200L)).build();
        assertThat(plant1).isEqualTo(plant2);
    }

    @Test
    @DisplayName("test idshould nameshould test Plant connection equals file falseshould file test")
    void plantNotEqualsTest() {
        Plant plant1 = Plant.builder().id(1L).name("test").build();
        Plant plant2 = Plant.builder().id(2L).name("test").build();
        Plant plant3 = Plant.builder().id(1L).name("test").build();
        assertThat(plant1).isNotEqualTo(plant2);
        assertThat(plant1).isNotEqualTo(plant3);
    }

    @Test
    @DisplayName("Plant test successfully should not connection successfully file test")
    void plantDefaultConstructorTest() {

        Plant plant = new Plant();

        assertThat(plant.getId()).isEqualTo(0L);
        assertThat(plant.getName()).isNull();
        assertThat(plant.getLocation()).isNull();
        assertThat(plant.getManager()).isNull();
        assertThat(plant.getEstablishedDate()).isNull();
    }
}
