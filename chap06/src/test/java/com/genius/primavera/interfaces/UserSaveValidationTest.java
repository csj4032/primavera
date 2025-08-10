package com.genius.primavera.interfaces;

import com.genius.primavera.application.UserService;
import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

@Testcontainers
@ActiveProfiles("test")
@DisplayName("user registration connection validation test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserSaveValidationTest {

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
    private TestRestTemplate restTemplate;
    
    @MockBean
    private UserService userService;

    @Test
    @Order(1)
    @DisplayName("user IDshould inquiry test")
    public void getUserById() {
        long body = restTemplate.getForObject("/users/1", long.class);
        Assertions.assertEquals(1L, body);
    }

    @Test
    @Order(2)
    @DisplayName("connection test connection validation")
    public void saveAndReturnUserIllegalEmail() {
        User source = User.builder().id(1L).email("genius@").password("Secret0!").nickname("genius").roles(List.of(new Role(1, RoleType.USER))).build();
        saveUser(source);
    }

    @Test
    @Order(3)
    @DisplayName("file connection test validation")
    public void saveAndReturnUserIllegalPassword() {
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0").nickname("genius").roles(List.of(new Role(1, RoleType.USER))).build();
        saveUser(source);
    }

    @Test
    @Order(4)
    @DisplayName("connection test test validation")
    public void saveAndReturnUserIllegalLongNickname() {
        String nickname = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname(nickname).roles(List.of(new Role(1, RoleType.USER))).build();
        saveUser(source);
    }

    @Test
    @Order(5)
    @DisplayName("connection test test validation")
    public void saveAndReturnUserIllegalShortNickname() {
        String nickname = "1";
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname(nickname).roles(List.of(new Role(1, RoleType.USER))).build();
        saveUser(source);
    }

    @Test
    @Order(6)
    @DisplayName("user test validation")
    public void saveAndReturnUserNotRole() {
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("genius").roles(null).build();
        saveUser(source);
    }

    @Test
    @Order(7)
    @DisplayName("connection test ID validation")
    public void saveAndReturnUserIllegalRoleId() {
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("genius").roles(List.of(new Role(0, RoleType.USER))).build();
        saveUser(source);
    }

    @Test
    @Order(8)
    @DisplayName("test test validation")
    public void saveAndReturnUserNullRoleType() {
        User source = User.builder()
                .id(1L)
                .email("genius@gmail.com")
                .password("Secret0!")
                .createdAt(Instant.now().plusSeconds(60 * 60 * 24))
                .updatedAt(Instant.now())
                .nickname("genius")
                .roles(List.of(new Role(1, null)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(10)
    @DisplayName("test user registration test - all test connection should 200 OK test")
    public void saveAndReturnUserValid() {
        String password = "ValidPass1!";
        User source = User.builder()
                .id(2L)
                .email("validuser" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("validnick")
                .roles(List.of(new Role(1, RoleType.USER)))
                .createdAt(Instant.now())
                .updatedAt(Instant.now().plusSeconds(10))
                .status(UserStatus.ACTIVE)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/save", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "test registration should 200 test 201 test");
    }

    @Test
    @Order(12)
    @DisplayName("test file should 400 Bad Request test")
    public void saveAndReturnUserEmptyRoles() {
        User source = User.builder()
                .id(4L)
                .email("emptyroles@gmail.com")
                .password("Secret0!")
                .nickname("emptyroles")
                .roles(List.of())
                .build();
        saveUser(source);
    }

    @Test
    @Order(13)
    @DisplayName("test nullneeds to be added 400 Bad Request test")
    public void saveAndReturnUserNullRoles() {
        User source = User.builder()
                .id(5L)
                .email("nullroles@gmail.com")
                .password("Secret0!")
                .nickname("nullroles")
                .roles(null)
                .build();
        saveUser(source);
    }

    @Test
    @Order(14)
    @DisplayName("connection nullneeds to be added 400 Bad Request test")
    public void saveAndReturnUserNullEmail() {
        User source = User.builder()
                .id(6L)
                .email(null)
                .password("Secret0!")
                .nickname("nullemail")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(15)
    @DisplayName("fileshould nullneeds to be added 400 Bad Request test")
    public void saveAndReturnUserNullPassword() {
        User source = User.builder()
                .id(7L)
                .email("nullpass@gmail.com")
                .password(null)
                .nickname("nullpass")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(16)
    @DisplayName("connection nullneeds to be added 400 Bad Request test")
    public void saveAndReturnUserNullNickname() {
        User source = User.builder()
                .id(8L)
                .email("nullnick@gmail.com")
                .password("Secret0!")
                .nickname(null)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(17)
    @DisplayName("connection file test should 400 Bad Request test")
    public void saveAndReturnUserNicknameWithSpecialChars() {
        User source = User.builder()
                .id(9L)
                .email("special@gmail.com")
                .password("Secret0!")
                .nickname("nick@name!")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(18)
    @DisplayName("connection test should 400 Bad Request test")
    public void saveAndReturnUserNicknameWithSpaces() {
        User source = User.builder()
                .id(10L)
                .email("spaces@gmail.com")
                .password("Secret0!")
                .nickname("nick name")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(19)
    @DisplayName("filetest should (7should) 400 Bad Request test")
    public void saveAndReturnUserPasswordTooShort() {
        User source = User.builder()
                .id(11L)
                .email("shortpw@gmail.com")
                .password("Short1!")
                .nickname("shortpw")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(20)
    @DisplayName("fileneeds to be added should (21should) 400 Bad Request test")
    public void saveAndReturnUserPasswordTooLong() {
        User source = User.builder()
                .id(12L)
                .email("longpw@gmail.com")
                .password("VeryLongPassword123!@")
                .nickname("longpw")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(21)
    @DisplayName("file test should 400 Bad Request test")
    public void saveAndReturnUserPasswordNoDigit() {
        User source = User.builder()
                .id(13L)
                .email("nodigit@gmail.com")
                .password("NoDigitPass!")
                .nickname("nodigit")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(22)
    @DisplayName("file test should 400 Bad Request test")
    public void saveAndReturnUserPasswordNoLowercase() {
        User source = User.builder()
                .id(14L)
                .email("nolower@gmail.com")
                .password("NOLOWER1!")
                .nickname("nolower")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(23)
    @DisplayName("file test should 400 Bad Request test")
    public void saveAndReturnUserPasswordNoUppercase() {
        User source = User.builder()
                .id(15L)
                .email("noupper@gmail.com")
                .password("noupper1!")
                .nickname("noupper")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(24)
    @DisplayName("file test should 400 Bad Request test")
    public void saveAndReturnUserPasswordNoSpecialChar() {
        User source = User.builder()
                .id(16L)
                .email("nospecial@gmail.com")
                .password("NoSpecial1")
                .nickname("nospecial")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(25)
    @DisplayName("file test should 400 Bad Request test")
    public void saveAndReturnUserPasswordWithSpaces() {
        User source = User.builder()
                .id(17L)
                .email("spacepw@gmail.com")
                .password("Space Pass1!")
                .nickname("spacepw")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(26)
    @DisplayName("test IDshould 0needs to be added 400 Bad Request test")
    public void saveAndReturnUserRoleIdZero() {
        User source = User.builder()
                .id(18L)
                .email("roleid0@gmail.com")
                .password("Secret0!")
                .nickname("roleid0")
                .roles(List.of(new Role(0, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(27)
    @DisplayName("test IDneeds to be added should 400 Bad Request test")
    public void saveAndReturnUserRoleIdNegative() {
        User source = User.builder()
                .id(19L)
                .email("roleidneg@gmail.com")
                .password("Secret0!")
                .nickname("roleidneg")
                .roles(List.of(new Role(-1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(28)
    @DisplayName("test connection test processing")
    public void saveAndReturnUserKoreanNickname() {
        String password = "Secret0!";
        User source = User.builder()
                .id(20L)
                .email("korean" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("testconnection")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/save", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "test connection test processing should");
    }

    private void saveUser(User source) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/save", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();

        Assertions.assertEquals(400, actualStatus, "Expected 400 Bad Request for invalid email");
    }
}