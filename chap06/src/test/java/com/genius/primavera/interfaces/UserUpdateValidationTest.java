package com.genius.primavera.interfaces;

import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@Testcontainers
@ActiveProfiles("test")
@DisplayName("user modification connection validation test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserUpdateValidationTest {

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

    @Test
    @Order(1)
    @DisplayName("user ID test validation")
    public void updateAndUserIllegalId() {
        User source = User.builder().email("genius@gmail.com").password("Secret0!").passwordConfirm("Secret0!").nickname("genius").status(UserStatus.INACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(2)
    @DisplayName("user test validation")
    public void saveAndReturnUserIllegalStatus() {
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("genius").status(null).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(3)
    @DisplayName("connection test test validation")
    public void saveAndReturnUserIllegalNickname() {
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("g").status(UserStatus.INACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(4)
    @DisplayName("user IDshould 0needs to be added 400 Bad Request test")
    public void updateAndUserIdZero() {
        User source = User.builder().id(0L).email("idZero@gmail.com").password("Secret0!").nickname("idZero").status(UserStatus.ACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(5)
    @DisplayName("user IDneeds to be added should 400 Bad Request test")
    public void updateAndUserIdNegative() {
        User source = User.builder().id(-1L).email("idNegative@gmail.com").password("Secret0!").nickname("idNegative").status(UserStatus.ACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(6)
    @DisplayName("should test error validation")
    public void updateAndUserInvalidEmail() {
        User source = User.builder().id(1L).email("invalid-email").password("Secret0!").nickname("invalidemail").status(UserStatus.ACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(7)
    @DisplayName("file connection test validation")
    public void updateAndUserInvalidPassword() {
        User source = User.builder().id(1L).email("invalidpw@gmail.com").password("simple").nickname("invalidpw").status(UserStatus.ACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(8)
    @DisplayName("connection test test validation")
    public void updateAndUserNicknameTooLong() {
        User source = User.builder().id(1L).email("toolong@gmail.com").password("Secret0!").nickname("should".repeat(21)).status(UserStatus.ACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(9)
    @DisplayName("test should file should 400 Bad Request test")
    public void updateAndUserEmptyRoles() {
        User source = User.builder().id(1L).email("emptyroles@gmail.com").password("Secret0!").nickname("emptyroles").status(UserStatus.ACTIVE).roles(List.of()).build();
        updateUser(source);
    }

    @Test
    @Order(10)
    @DisplayName("test nullneeds to be added 400 Bad Request test")
    public void updateAndUserNullRoles() {
        User source = User.builder().id(1L).email("nullroles@gmail.com").password("Secret0!").nickname("nullroles").status(UserStatus.ACTIVE).roles(null).build();
        updateUser(source);
    }

    @Test
    @Order(12)
    @DisplayName("needs to be added needs to be added 400 Bad Request test")
    public void updateAndUserEmptyEmail() {
        User source = User.builder().id(1L).email("").password("Secret0!").nickname("emptyemail").status(UserStatus.ACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(13)
    @DisplayName("fileneeds to be added needs to be added 400 Bad Request test")
    public void updateAndUserEmptyPassword() {
        User source = User.builder().id(1L).email("emptypass@gmail.com").password("").nickname("emptypass").status(UserStatus.ACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(14)
    @DisplayName("connection needs to be added should 400 Bad Request test")
    public void updateAndUserEmptyNickname() {
        User source = User.builder().id(1L).email("emptynick@gmail.com").password("Secret0!").nickname("").status(UserStatus.ACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(15)
    @DisplayName("connection connection test processing")
    public void updateAndUserKoreanOnlyNickname() {
        String password = "Secret0!";
        User source = User.builder()
                .id(2L)
                .email("korean" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("testconnection")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "test connection test processing should");
    }

    @Test
    @Order(16)
    @DisplayName("connection connection test processing")
    public void updateAndUserNumberOnlyNickname() {
        String password = "Secret0!";
        User source = User.builder()
                .id(3L)
                .email("numbers" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("12345")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "test connection test processing should");
    }

    @Test
    @Order(17)
    @DisplayName("connection connection test processing")
    public void updateAndUserEnglishOnlyNickname() {
        String password = "Secret0!";
        User source = User.builder()
                .id(4L)
                .email("english" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("englishonly")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "test connection test processing should");
    }

    @Test
    @Order(18)
    @DisplayName("user test DORMANTtest should test processing")
    public void updateAndUserStatusDormant() {
        String password = "Secret0!";
        User source = User.builder()
                .id(5L)
                .email("dormant" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("dormant")
                .status(UserStatus.DORMANT)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "DORMANT test test processing should");
    }

    @Test
    @Order(19)
    @DisplayName("user test INACTIVEtest should test processing")
    public void updateAndUserStatusInactive() {
        String password = "Secret0!";
        User source = User.builder()
                .id(6L)
                .email("inactive" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("inactive")
                .status(UserStatus.INACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "INACTIVE test test processing should");
    }

    @Test
    @Order(20)
    @DisplayName("test should user modification should test processing")
    public void updateAndUserMultipleRoles() {
        String password = "Secret0!";
        User source = User.builder()
                .id(7L)
                .email("multipleroles" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("multipleroles")
                .status(UserStatus.ACTIVE)
                .roles(List.of(
                        new Role(1, RoleType.USER),
                        new Role(2, RoleType.MANAGER)
                ))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "test test processing should");
    }

    @Test
    @Order(21)
    @DisplayName("test null connection test 400 Bad Request test")
    public void updateAndUserRoleWithNullType() {
        User source = User.builder().id(1L).email("nullroletype@gmail.com").password("Secret0!").nickname("nullroletype").status(UserStatus.ACTIVE).roles(List.of(new Role(1, null))).build();
        updateUser(source);
    }

    @Test
    @Order(22)
    @DisplayName("test IDshould connection test 400 Bad Request test")
    public void updateAndUserRoleNegativeId() {
        User source = User.builder().id(1L).email("negroleid@gmail.com").password("Secret0!").nickname("negroleid").status(UserStatus.ACTIVE).roles(List.of(new Role(-5, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(23)
    @DisplayName("test connection validation errorneeds to be added connection test 400 Bad Request test")
    public void updateAndUserMultipleValidationErrors() {
        User source = User.builder()
                .id(-1L)
                .email("invalid-email")
                .password("simple")
                .nickname("a")
                .status(null)
                .roles(List.of())
                .build();
        updateUser(source);
    }

    @Test
    @Order(24)
    @DisplayName("file created successfully test should 400 Bad Request test")
    public void updateAndUserPasswordMismatch() {
        User source = User.builder()
                .id(1L)
                .email("mismatch@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Different1!")
                .nickname("mismatch")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        updateUser(source);
    }

    @Test
    @Order(25)
    @DisplayName("file created successfully nullneeds to be added 400 Bad Request test")
    public void updateAndUserPasswordConfirmNull() {
        User source = User.builder()
                .id(1L)
                .email("confirmnull@gmail.com")
                .password("Secret0!")
                .passwordConfirm(null)
                .nickname("confirmnull")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        updateUser(source);
    }

    @Test
    @Order(26)
    @DisplayName("file created successfully needs to be added should 400 Bad Request test")
    public void updateAndUserPasswordConfirmEmpty() {
        User source = User.builder()
                .id(1L)
                .email("confirmempty@gmail.com")
                .password("Secret0!")
                .passwordConfirm("")
                .nickname("confirmempty")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        updateUser(source);
    }

    @Test
    @Order(27)
    @DisplayName("fileshould nulltest file created successfully test should 400 Bad Request test")
    public void updateAndUserPasswordNullConfirmExists() {
        User source = User.builder()
                .id(1L)
                .email("passnull@gmail.com")
                .password(null)
                .passwordConfirm("Secret0!")
                .nickname("passnull")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        updateUser(source);
    }

    @Test
    @Order(28)
    @DisplayName("file created successfully test nullneeds to be added 400 Bad Request test")
    public void updateAndUserBothPasswordsNull() {
        User source = User.builder()
                .id(1L)
                .email("bothpassnull@gmail.com")
                .password(null)
                .passwordConfirm(null)
                .nickname("bothpassnull")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        updateUser(source);
    }

    @Test
    @Order(29)
    @DisplayName("test user modification test - file should all should connection should 200 OK test")
    public void updateAndReturnUserValidWithMatchingPasswords() {
        String password = "ValidPass1!";
        User source = User.builder()
                .id(2L)
                .email("validupdate" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("validupdate")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "file needs to be added test modification should");
    }

    @Test
    @Order(30)
    @DisplayName("test user modification test - all should connection should 200 OK test")
    public void updateAndReturnUserValid() {
        String password = "ValidPass1!";
        User source = User.builder()
                .id(3L)
                .email("validupdate2" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("validupdate2")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "test modification should 200 test 201 test");
    }

    private void updateUser(User source) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        Assertions.assertEquals(400, destination.getStatusCode().value());
    }
}