package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.mapper.support.UserTableSupport;
import com.genius.primavera.domain.model.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.mybatis.dynamic.sql.SqlBuilder.isIn;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@DisplayName(value = "translated_text_2 translated_text_2 test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserMapperTest {

    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", mariadb::getDriverClassName);
    }

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    private static User source;
    private static List<User> users;
    private static List<User> bulkUsers;

    @BeforeAll
    public static void setUp() {
        users = new ArrayList<>();
        List<Role> roles = List.of(Role.builder().id(1).type(RoleType.USER).build());
        String password = new BCryptPasswordEncoder().encode("secret");
        UserStatus status = UserStatus.ACTIVE;
        long timestamp = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            users.add(User.builder()
                    .email("test_user_" + timestamp + "_" + i + "@example.com")
                    .nickname("testuser_" + timestamp + "_" + i)
                    .password(password)
                    .status(status)
                    .roles(roles)
                    .createdAt(Instant.now()).updatedAt(Instant.now()).build());
        }

        bulkUsers = new ArrayList<>();
        for (int i = 10; i < 100; i++) {
            bulkUsers.add(User.builder()
                    .email("bulk_user_" + timestamp + "_" + i + "@example.com")
                    .nickname("bulkuser_" + timestamp + "_" + i)
                    .password(password)
                    .status(status)
                    .roles(roles)
                    .createdAt(Instant.now()).updatedAt(Instant.now()).build());
        }

        source = User.builder().email("source_user_" + timestamp + "@example.com").nickname("sourceuser_" + timestamp).password(password).status(UserStatus.ACTIVE).roles(roles).createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    @Test
    @Order(1)
    @DisplayName(value = "translated_text_2 registration")
    public void save() {
        users.forEach(e -> userMapper.save(e));
    }

    @Test
    @Order(2)
    @DisplayName(value = "translated_text_2 registration translated_text_1 ID translated_text_1 translated_text_2")
    public void saveSelectKey() {
        userMapper.save(source);
        User destination = userMapper.findById(source.getId());
        Assertions.assertEquals(source.getEmail(), destination.getEmail());
    }

    @Test
    @Order(3)
    @DisplayName(value = "translated_text_2 translated_text_3 translated_text_2 translated_text_2")
    public void findById() {
        User user = userMapper.findById(source.getId());
        Assertions.assertEquals(source.getId(), user.getId());
    }

    @Test
    @Order(4)
    @DisplayName(value = "translated_text_2 translated_text_3 translated_text_2 modification")
    public void update() {
        source.setNickname("spring");
        source.setUpdatedAt(Instant.now());
        userMapper.update(source);
        User destination = userMapper.findById(source.getId());
        Assertions.assertEquals(destination.getNickname(), source.getNickname());
        Assertions.assertEquals(destination.getId(), source.getId());
    }

    @Test
    @Order(5)
    @DisplayName(value = "translated_text_2 translated_text_3 translated_text_2 deletion")
    public void deleteById() {
        int count = userMapper.deleteById(source.getId());
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(6)
    @DisplayName(value = "translated_text_2 translated_text_2 translated_text_2 translated_text_2")
    public void saveRoles() {
        for (User user : users)
            userRoleMapper.save(new UserRole(user.getId(), 1L));
    }

    @Test
    @Order(7)
    @DisplayName(value = "all translated_text_2 translated_text_2 translated_text_2 translated_text_2")
    public void findAllWithRoles() {
        List<User> destination = userMapper.findAll();

        Assertions.assertTrue(destination.size() >= users.size(),
                "translated_text_2 translated_text_2 translated_text_2 translated_text_4 translated_text_4. translated_text_2: " + users.size() + ", translated_text_2: " + destination.size());

        for (User expectedUser : users) {
            boolean found = destination.stream()
                    .anyMatch(user -> user.getEmail().equals(expectedUser.getEmail()));
            Assertions.assertTrue(found,
                    "translated_text_9 translated_text_2 translated_text_2 translated_text_1 translated_text_4: " + expectedUser.getEmail());
        }
    }

    @Test
    @Order(8)
    @DisplayName(value = "translated_text_2 translated_text_3 translated_text_2 result translated_text_2")
    public void findUserByRequestUser() {

        List<Long> validUserIds = users.stream()
                .filter(user -> user.getId() > 0)
                .map(User::getId)
                .collect(toList());

        if (validUserIds.isEmpty()) {
            log.warn("translated_text_3 user IDtranslated_text_1 translated_text_4. test translated_text_5.");
            return;
        }

        SelectStatementProvider selectStatement =
                select(UserTableSupport.id, UserTableSupport.email, UserTableSupport.password, UserTableSupport.nickname, UserTableSupport.status, UserTableSupport.createdAt, UserTableSupport.updatedAt)
                        .from(UserTableSupport.userTable)
                        .where(UserTableSupport.id, isIn(validUserIds))
                        .build()
                        .render(RenderingStrategies.MYBATIS3);
        List<User> destination = userMapper.findByRequestUser(selectStatement);

        Assertions.assertEquals(validUserIds.size(), destination.size(),
                "translated_text_3 translated_text_2 translated_text_1 translated_text_2 translated_text_2 translated_text_2 translated_text_4. translated_text_2: " + validUserIds.size() + ", translated_text_2: " + destination.size());

        for (User returnedUser : destination) {
            boolean found = validUserIds.contains(returnedUser.getId());
            Assertions.assertTrue(found,
                    "translated_text_2 translated_text_2 translated_text_2translated_text_1 translated_text_2: " + returnedUser.getEmail() + " (ID: " + returnedUser.getId() + ")");
        }

        log.info("translated_text_2 translated_text_2 result: {}", destination);
    }

    @Test
    @Order(9)
    @DisplayName(value = "translated_text_2 translated_text_2 registration")
    public void bulkSave() {
        userMapper.saveAll(bulkUsers);
        bulkUsers.stream().forEach(System.out::println);
    }
}