package com.genius.primavera.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.genius.primavera.dao.UserDao;
import com.genius.primavera.domain.User;
import com.genius.primavera.testcontainer.EnablePrimaveraTestcontainers;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(PrimaveraController.class)
@DisplayName("PrimaveraController 테스트")
@EnablePrimaveraTestcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PrimaveraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDao userDao;

    @MockBean
    private HikariDataSource dataSource;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @Order(1)
    @DisplayName("루트 및 인덱스 엔드포인트 테스트")
    public void testIndex() throws Exception {
        given(dataSource.getCatalog()).willReturn("test_catalog");
        mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk()).andExpect(content().string("test_catalog"));
        mockMvc.perform(get("/index")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    @Order(2)
    @DisplayName("사용자 목록 조회 테스트")
    void testGetUsers() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .email("user1@example.com")
                .nickname("UserOne")
                .password("pass1")
                .createdAt(Instant.parse("2023-01-01T10:00:00Z"))
                .updatedAt(Instant.parse("2023-01-01T10:00:00Z"))
                .build();
        User user2 = User.builder()
                .id(2L)
                .email("user2@example.com")
                .nickname("UserTwo")
                .password("pass2")
                .createdAt(Instant.parse("2023-01-02T11:00:00Z"))
                .updatedAt(Instant.parse("2023-01-02T11:00:00Z"))
                .build();
        List<User> expectedUsers = Arrays.asList(user1, user2);

        given(userDao.getUsers()).willReturn(expectedUsers);

        // when & then: "/users" 엔드포인트 호출 시
        mockMvc.perform(get("/users").contentType(MediaType.APPLICATION_JSON)) // 요청 Content-Type 명시
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // 응답 Content-Type이 JSON인지 확인
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].nickname").value("UserTwo"))
                // 전체 JSON 응답이 예상 객체 목록과 일치하는지 검증
                .andExpect(content().json(objectMapper.writeValueAsString(expectedUsers)));
    }

    @Test
    @Order(3)
    @DisplayName("단일 사용자 조회 성공 테스트")
    void testGetUserByIdSuccess() throws Exception {
        long userId = 1L;
        User expectedUser = User.builder()
                .id(userId)
                .email("single@example.com")
                .nickname("SingleUser")
                .password("single_pass")
                .createdAt(Instant.parse("2023-03-01T12:00:00Z"))
                .updatedAt(Instant.parse("2023-03-01T12:00:00Z"))
                .build();

        given(userDao.findById(userId)).willReturn(Optional.ofNullable(expectedUser));

        mockMvc.perform(get("/users/{id}", userId).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk()) // HTTP 상태 코드 200 OK 예상
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("single@example.com"))
                .andExpect(jsonPath("$.nickname").value("SingleUser"))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedUser)));
    }

    @Test
    @Order(4)
    @DisplayName("단일 사용자 조회 실패 테스트 (사용자 없음)")
    void testGetUserByIdNotFound() throws Exception {
        long userId = 99999L;
        given(userDao.findById(userId)).willReturn(Optional.empty());
        mockMvc.perform(get("/users/{id}", userId).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    @DisplayName("사용자 목록 조회 - 결과 없음 테스트")
    void testGetUsersEmpty() throws Exception {
        given(userDao.getUsers()).willReturn(Collections.emptyList());
        mockMvc.perform(get("/users").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]")); // 빈 JSON 배열 예상
    }
}