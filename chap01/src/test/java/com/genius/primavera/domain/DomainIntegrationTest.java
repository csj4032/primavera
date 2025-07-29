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
    @DisplayName("User, Role, Plant 객체 간의 상호작용이 정상적으로 이루어져야 한다")
    void domainIntegrationTest() {
        // given
        // 1. Role 생성
        Role adminRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("시스템 관리자")
                .build();

        Role managerRole = Role.builder()
                .id(2L)
                .name("PLANT_MANAGER")
                .description("공장 관리자")
                .build();

        // 2. 관리자 User 생성
        User manager = User.builder()
                .id(101L)
                .email("manager@example.com")
                .password("securePassword123")
                .nickname("공장장")
                .roles(Arrays.asList(adminRole, managerRole))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // 3. Plant 생성
        Plant plant = Plant.builder()
                .id(1L)
                .name("서울 공장")
                .location("서울시 강남구")
                .manager(manager)
                .establishedDate(LocalDateTime.of(2020, 1, 1, 0, 0).toInstant(ZoneOffset.UTC))
                .build();

        // when & then
        // 1. Plant의 관리자 확인
        assertThat(plant.getManager()).isEqualTo(manager);
        assertThat(plant.getManager().getEmail()).isEqualTo("manager@example.com");

        // 2. 관리자의 역할 확인
        List<Role> managerRoles = manager.getRoles();
        assertThat(managerRoles).hasSize(2);
        assertThat(managerRoles).contains(adminRole, managerRole);

        // 3. 특정 역할 확인
        boolean hasManagerRole = manager.getRoles().stream().anyMatch(role -> role.getName().equals("PLANT_MANAGER"));
        assertThat(hasManagerRole).isTrue();
    }

    @Test
    @DisplayName("여러 공장을 관리하는 관리자 설정 테스트")
    void multiPlantManagerTest() {
        // given
        // 1. Role 생성
        Role multiManagerRole = Role.builder()
                .id(3L)
                .name("MULTI_PLANT_MANAGER")
                .description("다수 공장 관리자")
                .build();

        // 2. 관리자 User 생성
        User manager = User.builder()
                .id(102L)
                .email("multi.manager@example.com")
                .password("securePassword456")
                .nickname("지역책임자")
                .roles(Arrays.asList(multiManagerRole))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // 3. 여러 Plant 생성
        Plant seoulPlant = Plant.builder()
                .id(1L)
                .name("서울 공장")
                .location("서울시 강남구")
                .manager(manager)
                .establishedDate(LocalDateTime.of(2020, 1, 1, 0, 0).toInstant(ZoneOffset.UTC))
                .build();

        Plant busanPlant = Plant.builder()
                .id(2L)
                .name("부산 공장")
                .location("부산시 해운대구")
                .manager(manager)
                .establishedDate(LocalDateTime.of(2021, 5, 15, 0, 0).toInstant(ZoneOffset.UTC))
                .build();

        // when & then
        // 1. 동일한 관리자 확인
        assertThat(seoulPlant.getManager()).isEqualTo(busanPlant.getManager());

        // 2. 관리자의 역할 확인
        assertThat(manager.getRoles()).hasSize(1);
        assertThat(manager.getRoles().get(0).getName()).isEqualTo("MULTI_PLANT_MANAGER");

        // 3. 서로 다른 공장 정보 확인
        assertThat(seoulPlant.getName()).isNotEqualTo(busanPlant.getName());
        assertThat(seoulPlant.getLocation()).isNotEqualTo(busanPlant.getLocation());
        assertThat(seoulPlant.getEstablishedDate()).isBefore(busanPlant.getEstablishedDate());
    }

    @Test
    @DisplayName("도메인 객체의 빌더와 toString 메서드 테스트")
    void domainToStringTest() {
        // given
        Role role = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("관리자")
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

        // when & then
        // 1. toString 메서드 호출 결과 확인
        assertThat(role.toString()).contains("id=1", "name=ADMIN", "description=관리자");
        assertThat(user.toString()).contains("id=1", "email=test@example.com");
        assertThat(plant.toString()).contains("id=1", "name=Test Plant");

        // 2. 객체 참조 관계 확인
        assertThat(plant.getManager()).isSameAs(user);
        assertThat(user.getRoles().get(0)).isSameAs(role);
    }
}
