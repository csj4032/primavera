package com.genius.primavera.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class PlantTest {

    @Test
    @DisplayName("Plant translated_text_3 successfully translated_text_11 translated_text_2")
    void plantCreationTest() {
        long id = 1L;
        String name = "translated_text_2 translated_text_2";
        String location = "translated_text_2 translated_text_3";
        User manager = new User(101L, "manager@example.com", "password", "translated_text_2", null, Instant.now(), Instant.now());
        Instant establishedDate = Instant.parse("2020-01-01T00:00:00Z");
        Plant plant = new Plant(id, name, location, manager, establishedDate);
        assertThat(plant.getId()).isEqualTo(id);
        assertThat(plant.getName()).isEqualTo(name);
        assertThat(plant.getLocation()).isEqualTo(location);
        assertThat(plant.getManager()).isEqualTo(manager);
        assertThat(plant.getEstablishedDate()).isEqualTo(establishedDate);
    }

    @Test
    @DisplayName("Plant Buildertranslated_text_1 translated_text_4 translated_text_1 successfully translated_text_10 translated_text_2")
    void plantBuilderTest() {
        long id = 1L;
        String name = "translated_text_2 translated_text_2";
        String location = "translated_text_2 translated_text_3";
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
    @DisplayName("translated_text_3 idtranslated_text_1 nametranslated_text_1 translated_text_2 Plant translated_text_3 equals translated_text_4 truetranslated_text_1 translated_text_4 translated_text_2")
    void plantEqualsTest() {
        Plant plant1 = Plant.builder().id(1L).name("translated_text_2 translated_text_2").build();
        Plant plant2 = Plant.builder().id(1L).name("translated_text_2 translated_text_2").location("translated_text_2 translated_text_2").manager(new User(200L)).build();
        assertThat(plant1).isEqualTo(plant2);
    }

    @Test
    @DisplayName("translated_text_2 idtranslated_text_1 nametranslated_text_1 translated_text_2 Plant translated_text_3 equals translated_text_4 falsetranslated_text_1 translated_text_4 translated_text_2")
    void plantNotEqualsTest() {
        Plant plant1 = Plant.builder().id(1L).name("translated_text_2 translated_text_2").build();
        Plant plant2 = Plant.builder().id(2L).name("translated_text_2 translated_text_2").build();
        Plant plant3 = Plant.builder().id(1L).name("translated_text_2 translated_text_2").build();
        assertThat(plant1).isNotEqualTo(plant2);
        assertThat(plant1).isNotEqualTo(plant3);
    }

    @Test
    @DisplayName("Plant translated_text_2 translated_text_10 translated_text_9 translated_text_3 successfully translated_text_4 translated_text_2")
    void plantDefaultConstructorTest() {

        Plant plant = new Plant();

        assertThat(plant.getId()).isEqualTo(0L);
        assertThat(plant.getName()).isNull();
        assertThat(plant.getLocation()).isNull();
        assertThat(plant.getManager()).isNull();
        assertThat(plant.getEstablishedDate()).isNull();
    }
}
