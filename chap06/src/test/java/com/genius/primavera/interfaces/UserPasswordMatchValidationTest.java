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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Order(3)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("translated_text_4 translated_text_2 validation test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserPasswordMatchValidationTest {

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
    @DisplayName("user registration translated_text_1 translated_text_4 translated_text_4 translated_text_13 translated_text_4 translated_text_2 translated_text_1 400 Bad Request translated_text_2")
    public void saveAndUserPasswordMismatch() {
        User source = User.builder()
                .id(1L)
                .email("mismatch@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Different1!")
                .nickname("mismatch")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(2)
    @DisplayName("user registration translated_text_1 translated_text_4 translated_text_3 translated_text_4 translated_text_13 translated_text_1 translated_text_4 translated_text_1 400 Bad Request translated_text_2")
    public void saveAndUserPasswordConfirmEmpty() {
        User source = User.builder()
                .id(1L)
                .email("confirmempty@gmail.com")
                .password("Secret0!")
                .passwordConfirm("")
                .nickname("confirmempty")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(3)
    @DisplayName("user registration translated_text_1 translated_text_4 nulltranslated_text_2 translated_text_4 translated_text_13 translated_text_2 translated_text_1 400 Bad Request translated_text_2")
    public void saveAndUserPasswordNullConfirmExists() {
        User source = User.builder()
                .id(1L)
                .email("passnull@gmail.com")
                .password(null)
                .passwordConfirm("Secret0!")
                .nickname("passnull")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(4)
    @DisplayName("user registration translated_text_1 translated_text_5 translated_text_2 translated_text_4 translated_text_3 400 Bad Request translated_text_2")
    public void saveAndUserPasswordCaseMismatch() {
        User source = User.builder()
                .id(1L)
                .email("casemismatch@gmail.com")
                .password("Secret0!")
                .passwordConfirm("secret0!")
                .nickname("casemismatch")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(5)
    @DisplayName("user registration translated_text_1 translated_text_2 translated_text_3 translated_text_4 translated_text_3 400 Bad Request translated_text_2")
    public void saveAndUserPasswordSpaceMismatch() {
        User source = User.builder()
                .id(1L)
                .email("spacemismatch@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Secret0! ")
                .nickname("spacemismatch")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(6)
    @DisplayName("user registration translated_text_1 translated_text_4 translated_text_2 translated_text_4 translated_text_3 400 Bad Request translated_text_2")
    public void saveAndUserPasswordSpecialCharMismatch() {
        User source = User.builder()
                .id(1L)
                .email("specialmismatch@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Secret0@")
                .nickname("specialmismatch")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(7)
    @DisplayName("user registration translated_text_1 translated_text_4 translated_text_4 translated_text_13 translated_text_3 translated_text_1 translated_text_2 processing")
    public void saveAndReturnUserValidWithMatchingPasswords() {
        String password = "ValidPass1!";
        User source = User.builder()
                .id(2L)
                .email("validmatch" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("validmatch")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/save", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        assertTrue(actualStatus == 200 || actualStatus == 201, "translated_text_4 translated_text_2 translated_text_1 translated_text_2 registeredtranslated_text_1 translated_text_1");
    }

    @Test
    @Order(8)
    @DisplayName("user modification translated_text_1 translated_text_4 translated_text_4 translated_text_13 translated_text_4 translated_text_2 translated_text_1 400 Bad Request translated_text_2")
    public void updateAndUserPasswordMismatch() {
        User source = User.builder()
                .id(1L)
                .email("updatemismatch@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Different1!")
                .nickname("updatemismatch")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        updateUser(source);
    }

    @Test
    @Order(9)
    @DisplayName("user modification translated_text_1 translated_text_4 translated_text_4 translated_text_13 translated_text_3 translated_text_1 translated_text_2 processing")
    public void updateAndReturnUserValidWithMatchingPasswords() {
        String password = "UpdatePass1!";
        User source = User.builder()
                .id(3L)
                .email("updatevalid" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("updatevalid")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        assertTrue(actualStatus == 200 || actualStatus == 201, "translated_text_4 translated_text_2 translated_text_1 translated_text_2 modificationtranslated_text_1 translated_text_1");
    }

    private void saveUser(User source) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/save", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        assertEquals(400, actualStatus, "translated_text_4 translated_text_3 translated_text_1 400 Bad Request translated_text_2");
    }

    private void updateUser(User source) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        assertEquals(400, actualStatus, "translated_text_4 translated_text_3 translated_text_1 400 Bad Request translated_text_2");
    }
}