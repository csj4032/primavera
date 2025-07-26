package com.genius.primavera.interfaces;

import com.genius.primavera.application.NotFoundUserException;
import com.genius.primavera.application.UserService;
import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Integration test converted to unit test - needs service layer mocking")
public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserController userController;

    @Test
    @Order(1)
    public void findByIdTest() {
        User user = restTemplate.getForObject("/users/1", User.class);
        Assertions.assertEquals(1, user.getId());
    }

    @Test
    @Order(2)
    @DisplayName("유저 등록")
    public void saveTest() {
        User source = User.builder().email("genius@gmail.com").password("Secret0!").nickname("genius").roles(List.of(new Role(1, RoleType.USER))).build();
        Assertions.assertThrows(SQLIntegrityConstraintViolationException.class, () -> {
            ResponseEntity<User> responseEntity = restTemplate.postForEntity("/users/save", source, User.class);
            Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
            User destination = responseEntity.getBody();
            Assertions.assertEquals(destination.getEmail(), source.getEmail());
        });
    }

    @Test
    @Order(3)
    @DisplayName("유저 수정")
    public void updateTest() {
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("spring").status(UserStatus.ON).roles(List.of(new Role(1, RoleType.USER))).build();
        ResponseEntity<User> responseEntity = restTemplate.postForEntity("/users/update", source, User.class);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        User destination = responseEntity.getBody();
        Assertions.assertEquals(destination.getNickname(), source.getNickname());
    }

    @Test
    @Order(4)
    @DisplayName("미존재 유저 수정")
    public void updateNotFoundUserTest() {
        User source = User.builder().id(1000000L).email("genius@gmail.com").password("Secret0!").nickname("spring").status(UserStatus.ON).roles(List.of(new Role(1, RoleType.USER))).build();
        Assertions.assertThrows(RestClientException.class, () -> restTemplate.postForEntity("/users/update", source, User.class));
    }
}