package com.genius.primavera.testContainer;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("컨테이너 타입 열거형 테스트")
class ContainerTypeTest {

    @Test
    @DisplayName("모든 컨테이너 타입이 올바른 Docker 이미지를 가지는지 확인")
    void shouldHaveCorrectDockerImages() {
        assertEquals("mariadb:11.4.7", ContainerType.MARIADB.getDockerImage());
        assertEquals("redis:7-alpine", ContainerType.REDIS.getDockerImage());
        assertEquals("confluentinc/cp-kafka:latest", ContainerType.KAFKA.getDockerImage());
        assertEquals("postgres:15-alpine", ContainerType.POSTGRESQL.getDockerImage());
        
        log.info("All container types have valid Docker images");
    }

    @Test
    @DisplayName("모든 컨테이너 타입이 올바른 기본 포트를 가지는지 확인")
    void shouldHaveCorrectDefaultPorts() {
        assertEquals(3306, ContainerType.MARIADB.getDefaultPort());
        assertEquals(6379, ContainerType.REDIS.getDefaultPort());
        assertEquals(9092, ContainerType.KAFKA.getDefaultPort());
        assertEquals(5432, ContainerType.POSTGRESQL.getDefaultPort());
        
        log.info("All container types have valid default ports");
    }

    @Test
    @DisplayName("컨테이너 타입 열거형이 예상된 수의 값을 가지는지 확인")
    void shouldHaveExpectedNumberOfContainerTypes() {
        ContainerType[] types = ContainerType.values();
        assertEquals(4, types.length);
        
        // 각 타입이 존재하는지 확인
        assertNotNull(ContainerType.valueOf("MARIADB"));
        assertNotNull(ContainerType.valueOf("REDIS"));
        assertNotNull(ContainerType.valueOf("KAFKA"));
        assertNotNull(ContainerType.valueOf("POSTGRESQL"));
        
        log.info("Found {} container types: {}", types.length, (Object) types);
    }

    @Test
    @DisplayName("컨테이너 타입별 설정 정보 로깅")
    void shouldLogContainerTypeInformation() {
        for (ContainerType type : ContainerType.values()) {
            log.info("Container Type: {}", type.name());
            log.info("  - Docker Image: {}", type.getDockerImage());
            log.info("  - Default Port: {}", type.getDefaultPort());
        }
    }
}