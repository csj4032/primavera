package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest(properties = {"spring.datasource.driver-class-name=org.mariadb.jdbc.Driver", "mybatis.mapper-locations=classpath:mapper/**/*.xml"})
@ActiveProfiles(value = "test")
@ExtendWith(SpringExtension.class)
@DisplayName(value = "권한 관련 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class RoleMapperTest {

    public static final String USER_NAME = "primavera";
    public static final String PASS_WORLD = "primavera";
    public static final String CATALOG = "primavera";

    @Container
    private static final MariaDBContainer<?> mysqlContainer = new MariaDBContainer<>("mariadb:11.4.7")
            .withDatabaseName(CATALOG)
            .withUsername(USER_NAME)
            .withPassword(PASS_WORLD)
            .withInitScript("sql/schema.sql");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        String jdbcUrl = mysqlContainer.getJdbcUrl() + "?allowPublicKeyRetrieval=true&useSSL=false";
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }


    @Autowired
    private RoleMapper roleMapper;

    @BeforeAll
    @DisplayName("setUp")
    public static void setUp() {
        log.info("MySQL 컨테이너 테스트 설정 완료");
    }

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