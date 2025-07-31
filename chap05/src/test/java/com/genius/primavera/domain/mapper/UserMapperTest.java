package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.mapper.support.UserTableSupport;
import com.genius.primavera.domain.model.*;
import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.mybatis.dynamic.sql.SqlBuilder.isIn;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnablePrimaveraTestcontainers
@DisplayName(value = "유저 관련 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserMapperTest {

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
    @DisplayName(value = "유저 등록")
    public void save() {
        users.forEach(e -> userMapper.save(e));
    }

    @Test
    @Order(2)
    @DisplayName(value = "유저 등록 후 ID 값 반환")
    public void saveSelectKey() {
        userMapper.save(source);
        User destination = userMapper.findById(source.getId());
        Assertions.assertEquals(source.getEmail(), destination.getEmail());
    }

    @Test
    @Order(3)
    @DisplayName(value = "특정 아이디 유저 검색")
    public void findById() {
        User user = userMapper.findById(source.getId());
        Assertions.assertEquals(source.getId(), user.getId());
    }

    @Test
    @Order(4)
    @DisplayName(value = "특정 아이디 유저 수정")
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
    @DisplayName(value = "특정 아이디 유저 삭제")
    public void deleteById() {
        int count = userMapper.deleteById(source.getId());
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(6)
    @DisplayName(value = "특정 유저 권한 저장")
    public void saveRoles() {
        for (User user : users)
            userRoleMapper.save(new UserRole(user.getId(), 1L));
    }

    @Test
    @Order(7)
    @DisplayName(value = "모든 유저 권한 포함 검색")
    public void findAllWithRoles() {
        List<User> destination = userMapper.findAll();
        // 데이터베이스에 다른 테스트에서 생성된 추가 유저가 있을 수 있으므로
        // 정확한 매치 대신 우리가 생성한 유저들이 포함되어 있는지 확인
        Assertions.assertTrue(destination.size() >= users.size(), 
            "검색된 유저 수가 예상보다 적습니다. 예상: " + users.size() + ", 실제: " + destination.size());
        
        // 우리가 생성한 각 유저가 결과에 포함되어 있는지 확인 (이메일로 비교)
        for (User expectedUser : users) {
            boolean found = destination.stream()
                .anyMatch(user -> user.getEmail().equals(expectedUser.getEmail()));
            Assertions.assertTrue(found, 
                "생성한 유저를 찾을 수 없습니다: " + expectedUser.getEmail());
        }
    }

    @Test
    @Order(8)
    @DisplayName(value = "검색 조건에 따른 결과 반환")
    public void findUserByRequestUser() {
        SelectStatementProvider selectStatement =
                select(UserTableSupport.id, UserTableSupport.email, UserTableSupport.password, UserTableSupport.nickname, UserTableSupport.status, UserTableSupport.createdAt, UserTableSupport.updatedAt)
                        .from(UserTableSupport.userTable)
                        .where(UserTableSupport.id, isIn(users.stream().map(User::getId).collect(toList())))
                        .build()
                        .render(RenderingStrategies.MYBATIS3);
        List<User> destination = userMapper.findByRequestUser(selectStatement);
        
        // 요청한 ID들에 해당하는 유저들이 모두 반환되었는지 확인
        Assertions.assertEquals(users.size(), destination.size(), 
            "요청한 유저 수와 반환된 유저 수가 다릅니다.");
        
        // 반환된 모든 유저가 우리가 요청한 유저들 중 하나인지 확인
        for (User returnedUser : destination) {
            boolean found = users.stream()
                .anyMatch(user -> user.getId() == returnedUser.getId());
            Assertions.assertTrue(found, 
                "예상하지 않은 유저가 반환되었습니다: " + returnedUser.getEmail());
        }
        
        log.info("조건 검색 결과: {}", destination);
    }

    @Test
    @Order(9)
    @DisplayName(value = "유저 벌크 등록")
    public void bulkSave() {
        userMapper.saveAll(bulkUsers);
        bulkUsers.stream().forEach(System.out::println);
    }
}