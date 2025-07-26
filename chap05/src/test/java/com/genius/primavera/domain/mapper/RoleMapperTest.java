package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import com.genius.primavera.test.annotation.PrimaveraTestContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
@ActiveProfiles(value = "test")
@ExtendWith(SpringExtension.class)
@DisplayName(value = "권한 관련 테스트")
@PrimaveraTestContainer
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoleMapperTest {

    @Autowired
    private RoleMapper roleMapper;

    @Test
    @Order(1)
    @DisplayName("권한 저장 테스트")
    public void save() {
        List<Role> roles = new ArrayList<>();
        roles.add(Role.builder().type(RoleType.USER).build());
        roles.add(Role.builder().type(RoleType.MANAGER).build());
        roles.add(Role.builder().type(RoleType.ADMINISTRATOR).build());
        roles.forEach(role -> {
            int result = roleMapper.save(role);
            log.info("Role Insert Result : {}", result);
            Assertions.assertEquals(1, result);
        });
    }
}