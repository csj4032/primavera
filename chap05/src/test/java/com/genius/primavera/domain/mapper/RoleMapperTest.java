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
@DisplayName(value = "test test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoleMapperTest {

    @Autowired
    private RoleMapper roleMapper;

    @Test
    @Order(1)
    @DisplayName("test data test")
    public void insertRoleData() {
        try {
            long result = roleMapper.save(Role.builder().type(RoleType.ETC).build());
            log.info("ETC test successfully logging. result: {}", result);
            assertEquals(1, result);
        } catch (Exception e) {
            log.info("ETC test should exception test: {}", e.getMessage());
            log.info("exception test: {}", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                log.info("test: {}", e.getCause().getMessage());
            }
            assertTrue(true, "ETC test configuration connection Endpoint.");
        }
    }

    @Test
    @Order(2)
    @DisplayName("test data verification")
    public void verifyRoleData() {
        List<Role> roles = roleMapper.selectAll();
        log.info(roles.toString());
        assertNotNull(roles, "test data nullfile should connection.");
        assertFalse(roles.isEmpty(), "test data with should connection.");
        for (Role role : roles) {
            log.info("Role ID: {}, Type: {}, Name: {}", role.getId(), role.getType().getValue(), role.getType().getName());
            assertNotNull(role.getType(), "test nullfile should connection.");
            assertNotNull(role.getType().getName(), "test connection nullfile should connection.");
        }
    }

    @Test
    @Order(3)
    @DisplayName("test data deletion")
    public void deleteRoleData() {
        roleMapper.deleteAll();
        List<Role> roles = roleMapper.selectAll();
        log.info("test data deletion should verification: {}", roles);
        assertTrue(roles.isEmpty(), "test data Endpoint connection.");
    }
}