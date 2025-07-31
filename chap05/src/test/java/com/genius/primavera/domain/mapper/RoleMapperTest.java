package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName(value = "권한 관련 테스트")
@EnablePrimaveraTestcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoleMapperTest {

    @Autowired
    private RoleMapper roleMapper;

    @Test
    @Order(1)
    @DisplayName("기타 권한 데이터 삽입")
    public void insertRoleData() {
        roleMapper.save(Role.builder().type(RoleType.ETC).build());
    }

    @Test
    @Order(2)
    @DisplayName("권한 데이터 확인")
    public void verifyRoleData() {
        List<Role> roles = roleMapper.selectAll();
        log.info(roles.toString());
        Assertions.assertNotNull(roles, "권한 데이터가 null이어서는 안 됩니다.");
        Assertions.assertFalse(roles.isEmpty(), "권한 데이터가 비어있어서는 안 됩니다.");
        for (Role role : roles) {
            log.info("Role ID: {}, Type: {}, Name: {}", role.getId(), role.getType().getValue(), role.getType().getName());
            Assertions.assertNotNull(role.getType(), "권한 타입이 null이어서는 안 됩니다.");
            Assertions.assertNotNull(role.getType().getName(), "권한 이름이 null이어서는 안 됩니다.");
        }
    }
}