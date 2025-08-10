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
@DisplayName(value = "translated_text_2 translated_text_2 test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoleMapperTest {

    @Autowired
    private RoleMapper roleMapper;

    @Test
    @Order(1)
    @DisplayName("translated_text_2 translated_text_2 data translated_text_2")
    public void insertRoleData() {
        try {
            long result = roleMapper.save(Role.builder().type(RoleType.ETC).build());
            log.info("ETC translated_text_2 translated_text_10 translated_text_7. result: {}", result);
            assertEquals(1, result);
        } catch (Exception e) {
            log.info("ETC translated_text_2 translated_text_2 translated_text_1 exception translated_text_2: {}", e.getMessage());
            log.info("exception translated_text_2: {}", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                log.info("translated_text_2: {}", e.getCause().getMessage());
            }
            assertTrue(true, "ETC translated_text_2 translated_text_2 translated_text_8 translated_text_3 translated_text_5.");
        }
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_2 data verification")
    public void verifyRoleData() {
        List<Role> roles = roleMapper.selectAll();
        log.info(roles.toString());
        assertNotNull(roles, "translated_text_2 data nulltranslated_text_4 translated_text_1 translated_text_3.");
        assertFalse(roles.isEmpty(), "translated_text_2 data translated_text_6 translated_text_1 translated_text_3.");
        for (Role role : roles) {
            log.info("Role ID: {}, Type: {}, Name: {}", role.getId(), role.getType().getValue(), role.getType().getName());
            assertNotNull(role.getType(), "translated_text_2 translated_text_2 nulltranslated_text_4 translated_text_1 translated_text_3.");
            assertNotNull(role.getType().getName(), "translated_text_2 translated_text_3 nulltranslated_text_4 translated_text_1 translated_text_3.");
        }
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_2 data deletion")
    public void deleteRoleData() {
        roleMapper.deleteAll();
        List<Role> roles = roleMapper.selectAll();
        log.info("translated_text_2 data deletion translated_text_1 verification: {}", roles);
        assertTrue(roles.isEmpty(), "translated_text_2 data translated_text_5 translated_text_3.");
    }
}