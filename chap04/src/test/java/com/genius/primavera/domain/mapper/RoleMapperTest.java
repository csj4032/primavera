package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
@ActiveProfiles(value = "test")
@ExtendWith(SpringExtension.class)
@DisplayName(value = "권한 관련 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = "/sql/role.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class RoleMapperTest {

    @Autowired
    private RoleMapper RoleMapper;

    @BeforeAll
    @DisplayName("setUp")
    public static void setUp() {
        log.info("setUp");
    }

    @Test
    @DisplayName("권한 저장 테스트")
    public void save() {
        List<Role> roles = new ArrayList<>();
        roles.add(Role.builder().type(RoleType.USER).build());
        roles.add(Role.builder().type(RoleType.MANAGER).build());
        roles.add(Role.builder().type(RoleType.ADMINISTRATOR).build());
        roles.forEach(role -> {
            int result = RoleMapper.save(role);
            log.info("Role Insert Result : {}", result);
        });
    }
}