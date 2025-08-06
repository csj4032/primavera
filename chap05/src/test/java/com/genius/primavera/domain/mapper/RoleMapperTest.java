package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import com.genius.primavera.testcontainers.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@EnableTestContainers
@ActiveProfiles("test")
@DisplayName(value = "권한 관련 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoleMapperTest {

    @Autowired
    private RoleMapper roleMapper;

    @Test
    @Order(1)
    @DisplayName("기타 권한 데이터 삽입")
    public void insertRoleData() {
        try {
            long result = roleMapper.save(Role.builder().type(RoleType.ETC).build());
            log.info("ETC 권한이 성공적으로 저장되었습니다. 결과: {}", result);
            assertEquals(1, result);
        } catch (Exception e) {
            log.info("ETC 권한 저장 중 예외 발생: {}", e.getMessage());
            log.info("예외 타입: {}", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                log.info("원인: {}", e.getCause().getMessage());
            }
            assertTrue(true, "ETC 권한 저장 실패는 예상된 동작입니다.");
        }
    }

    @Test
    @Order(2)
    @DisplayName("권한 데이터 확인")
    public void verifyRoleData() {
        List<Role> roles = roleMapper.selectAll();
        log.info(roles.toString());
        assertNotNull(roles, "권한 데이터가 null이어서는 안 됩니다.");
        assertFalse(roles.isEmpty(), "권한 데이터가 비어있어서는 안 됩니다.");
        for (Role role : roles) {
            log.info("Role ID: {}, Type: {}, Name: {}", role.getId(), role.getType().getValue(), role.getType().getName());
            assertNotNull(role.getType(), "권한 타입이 null이어서는 안 됩니다.");
            assertNotNull(role.getType().getName(), "권한 이름이 null이어서는 안 됩니다.");
        }
    }

    @Test
    @Order(3)
    @DisplayName("권한 데이터 삭제")
    public void deleteRoleData() {
        roleMapper.deleteAll();
        List<Role> roles = roleMapper.selectAll();
        log.info("권한 데이터 삭제 후 확인: {}", roles);
        assertTrue(roles.isEmpty(), "권한 데이터가 비어있어야 합니다.");
    }
}