package com.genius.primavera.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class PlantTest {

    @Test
    @DisplayName("Plant 객체가 정상적으로 생성되어야 한다")
    void plantCreationTest() {
        // given
        long id = 1L;
        String name = "서울 공장";
        String location = "서울시 강남구";
        User manager = new User(101L, "manager@example.com", "password", "공장장", null, LocalDateTime.now(), LocalDateTime.now());
        LocalDateTime establishedDate = LocalDateTime.of(2020, 1, 1, 0, 0);

        // when
        Plant plant = new Plant(id, name, location, manager, establishedDate);

        // then
        assertThat(plant.getId()).isEqualTo(id);
        assertThat(plant.getName()).isEqualTo(name);
        assertThat(plant.getLocation()).isEqualTo(location);
        assertThat(plant.getManager()).isEqualTo(manager);
        assertThat(plant.getEstablishedDate()).isEqualTo(establishedDate);
    }

    @Test
    @DisplayName("Plant Builder를 사용하여 객체를 정상적으로 생성해야 한다")
    void plantBuilderTest() {
        // given
        long id = 1L;
        String name = "서울 공장";
        String location = "서울시 강남구";
        User manager = User.builder().id(101L).email("manager@example.com").build();
        LocalDateTime establishedDate = LocalDateTime.of(2020, 1, 1, 0, 0);

        // when
        Plant plant = Plant.builder()
                .id(id)
                .name(name)
                .location(location)
                .manager(manager)
                .establishedDate(establishedDate)
                .build();

        // then
        assertThat(plant.getId()).isEqualTo(id);
        assertThat(plant.getName()).isEqualTo(name);
        assertThat(plant.getLocation()).isEqualTo(location);
        assertThat(plant.getManager().getId()).isEqualTo(manager.getId());
        assertThat(plant.getEstablishedDate()).isEqualTo(establishedDate);
    }

    @Test
    @DisplayName("동일한 id와 name을 가진 Plant 객체는 equals 비교에서 true를 반환해야 한다")
    void plantEqualsTest() {
        // given
        Plant plant1 = Plant.builder()
                .id(1L)
                .name("서울 공장")
                .build();

        Plant plant2 = Plant.builder()
                .id(1L)
                .name("서울 공장")
                .location("다른 위치")
                .manager(new User(200L))
                .build();

        // when & then
        assertThat(plant1).isEqualTo(plant2);
    }

    @Test
    @DisplayName("다른 id나 name을 가진 Plant 객체는 equals 비교에서 false를 반환해야 한다")
    void plantNotEqualsTest() {
        // given
        Plant plant1 = Plant.builder()
                .id(1L)
                .name("서울 공장")
                .build();

        Plant plant2 = Plant.builder()
                .id(2L)
                .name("서울 공장")
                .build();

        Plant plant3 = Plant.builder()
                .id(1L)
                .name("부산 공장")
                .build();

        // when & then
        assertThat(plant1).isNotEqualTo(plant2);
        assertThat(plant1).isNotEqualTo(plant3);
    }
}
