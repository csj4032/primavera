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
@DisplayName("user registration translated_text_3 validation test")
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
    @DisplayName("user IDtranslated_text_1 inquiry test")
    public void getUserById() {
        long body = restTemplate.getForObject("/users/1", long.class);
        Assertions.assertEquals(1L, body);
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_3 translated_text_3 translated_text_2 translated_text_3 validation")
    public void saveAndReturnUserIllegalEmail() {
        User source = User.builder().id(1L).email("genius@").password("Secret0!").nickname("genius").roles(List.of(new Role(1, RoleType.USER))).build();
        saveUser(source);
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_4 translated_text_3 translated_text_2 translated_text_2 validation")
    public void saveAndReturnUserIllegalPassword() {
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0").nickname("genius").roles(List.of(new Role(1, RoleType.USER))).build();
        saveUser(source);
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_3 translated_text_2 translated_text_2 translated_text_2 validation")
    public void saveAndReturnUserIllegalLongNickname() {
        String nickname = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname(nickname).roles(List.of(new Role(1, RoleType.USER))).build();
        saveUser(source);
    }

    @Test
    @Order(5)
    @DisplayName("translated_text_3 translated_text_2 translated_text_2 translated_text_2 validation")
    public void saveAndReturnUserIllegalShortNickname() {
        String nickname = "1";
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname(nickname).roles(List.of(new Role(1, RoleType.USER))).build();
        saveUser(source);
    }

    @Test
    @Order(6)
    @DisplayName("user translated_text_2 translated_text_2 validation")
    public void saveAndReturnUserNotRole() {
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("genius").roles(null).build();
        saveUser(source);
    }

    @Test
    @Order(7)
    @DisplayName("translated_text_3 translated_text_2 ID validation")
    public void saveAndReturnUserIllegalRoleId() {
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("genius").roles(List.of(new Role(0, RoleType.USER))).build();
        saveUser(source);
    }

    @Test
    @Order(8)
    @DisplayName("translated_text_2 translated_text_2 translated_text_2 validation")
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
    @DisplayName("translated_text_2 user registration translated_text_2 - all translated_text_2 translated_text_3 translated_text_1 200 OK translated_text_2")
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
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "translated_text_2 registration translated_text_1 200 translated_text_2 201 translated_text_2");
    }

    @Test
    @Order(12)
    @DisplayName("translated_text_2 translated_text_4 translated_text_4 translated_text_1 400 Bad Request translated_text_2")
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
    @DisplayName("translated_text_2 nulltranslated_text_1 translated_text_1 400 Bad Request translated_text_2")
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
    @DisplayName("translated_text_3 nulltranslated_text_1 translated_text_1 400 Bad Request translated_text_2")
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
    @DisplayName("translated_text_4translated_text_1 nulltranslated_text_1 translated_text_1 400 Bad Request translated_text_2")
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
    @DisplayName("translated_text_3 nulltranslated_text_1 translated_text_1 400 Bad Request translated_text_2")
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
    @DisplayName("translated_text_3 translated_text_4 translated_text_2 translated_text_1 400 Bad Request translated_text_2")
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
    @DisplayName("translated_text_3 translated_text_2 translated_text_2 translated_text_1 400 Bad Request translated_text_2")
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
    @DisplayName("translated_text_4translated_text_1 translated_text_2 translated_text_1 (7translated_text_1) 400 Bad Request translated_text_2")
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
    @DisplayName("translated_text_4translated_text_1 translated_text_1 translated_text_1 (21translated_text_1) 400 Bad Request translated_text_2")
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
    @DisplayName("translated_text_4 translated_text_1 translated_text_2 translated_text_1 400 Bad Request translated_text_2")
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
    @DisplayName("translated_text_4 translated_text_1 translated_text_2 translated_text_1 400 Bad Request translated_text_2")
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
    @DisplayName("translated_text_4 translated_text_1 translated_text_2 translated_text_1 400 Bad Request translated_text_2")
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
    @DisplayName("translated_text_4 translated_text_4 translated_text_2 translated_text_1 400 Bad Request translated_text_2")
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
    @DisplayName("translated_text_4 translated_text_2 translated_text_2 translated_text_1 400 Bad Request translated_text_2")
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
    @DisplayName("translated_text_2 IDtranslated_text_1 0translated_text_1 translated_text_1 400 Bad Request translated_text_2")
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
    @DisplayName("translated_text_2 IDtranslated_text_1 translated_text_1 translated_text_1 400 Bad Request translated_text_2")
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
    @DisplayName("translated_text_2 translated_text_3 translated_text_2 processing")
    public void saveAndReturnUserKoreanNickname() {
        String password = "Secret0!";
        User source = User.builder()
                .id(20L)
                .email("korean" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("translated_text_2translated_text_3")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/save", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "translated_text_2 translated_text_3 translated_text_2 processing translated_text_1");
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