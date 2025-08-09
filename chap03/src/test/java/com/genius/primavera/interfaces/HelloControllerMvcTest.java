package com.genius.primavera.interfaces;

import com.genius.primavera.applicaiton.HelloService;
import com.genius.primavera.applicaiton.OopsException;
import com.genius.primavera.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@WebMvcTest(HelloController.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Spring MVC HelloController 테스트")
public class HelloControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HelloService helloService;

    @Test
    @Order(1)
    @DisplayName("GET /hello - 기본 MVC 매핑 테스트")
    void testHelloWorldMapping() throws Exception {
        List<User> mockUsers = List.of(
                User.builder().id(1L).name("User1").email("user1@test.com").build(),
                User.builder().id(2L).name("User2").email("user2@test.com").build());
        when(helloService.getUsers()).thenReturn(mockUsers);
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("hello"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("users", mockUsers))
                .andDo(print());
        log.info("✅ 기본 MVC 매핑 테스트 완료");
    }

    @Test
    @Order(2)
    @DisplayName("GET /world/{id} - 경로 변수 바인딩 테스트")
    void testPathVariableBinding() throws Exception {
        Long userId = 123L;
        User mockUser = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@example.com")
                .build();
        when(helloService.getUserById(userId)).thenReturn(mockUser);

        mockMvc.perform(get("/world/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("world"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", mockUser))
                .andDo(print());

        log.info("✅ 경로 변수 바인딩 테스트 완료");
    }

    @Test
    @Order(3)
    @DisplayName("GET /oops - 예외 처리 테스트")
    void testExceptionHandling() throws Exception {
        mockMvc.perform(get("/oops")).andExpect(status().isInternalServerError()).andDo(print());
        log.info("✅ 예외 처리 테스트 완료");
    }

    @Test
    @Order(4)
    @DisplayName("GET /order - 일반 뷰 반환 테스트")
    void testSimpleViewReturn() throws Exception {
        mockMvc.perform(get("/order")).andExpect(status().isOk()).andExpect(view().name("hello")).andDo(print());
        log.info("✅ 일반 뷰 반환 테스트 완료");
    }


    @Test
    @Order(5)
    @DisplayName("Model 데이터 검증 테스트")
    void testModelDataValidation() throws Exception {
        List<User> mockUsers = Arrays.asList(
                User.builder().id(1L).name("Alice").email("alice@test.com").build(),
                User.builder().id(2L).name("Bob").email("bob@test.com").build()
        );
        when(helloService.getUsers()).thenReturn(mockUsers);
        MvcResult result = mockMvc.perform(get("/hello")).andExpect(status().isOk()).andReturn();
        Object modelAttribute = result.getModelAndView().getModel().get("users");
        assertThat(modelAttribute).isNotNull();
        assertThat(modelAttribute).isInstanceOf(List.class);
        List<User> users = (List<User>) modelAttribute;
        assertThat(users).hasSize(2);
        assertThat(users.get(0).getName()).isEqualTo("Alice");
        log.info("✅ Model 데이터 검증 테스트 완료");
    }

    @Test
    @Order(6)
    @DisplayName("요청 파라미터와 경로 변수 복합 테스트")
    void testComplexRequestMapping() throws Exception {
        Long userId = 456L;
        User mockUser = User.builder().id(userId).name("Complex User").build();
        when(helloService.getUserById(userId)).thenReturn(mockUser);
        mockMvc.perform(get("/world/{id}", userId)
                        .param("debug", "true")
                        .param("format", "html"))
                .andExpect(status().isOk())
                .andExpect(view().name("world"))
                .andDo(print());
        log.info("✅ 복합 요청 매핑 테스트 완료");
    }

    @Test
    @Order(7)
    @DisplayName("MVC 아키텍처 컴포넌트 검증")
    void testMvcArchitectureComponents() throws Exception {
        when(helloService.getUsers()).thenReturn(Collections.emptyList());
        MvcResult result = mockMvc.perform(get("/hello")).andExpect(status().isOk()).andReturn();
        assertThat(result.getHandler()).isNotNull();
        assertThat(result.getModelAndView()).isNotNull();
        assertThat(result.getModelAndView().getViewName()).isEqualTo("hello");
        assertThat(result.getModelAndView().getModel()).isNotEmpty();
        log.info("✅ MVC 아키텍처 컴포넌트 검증 완료");
    }

    @Test
    @Order(8)
    @DisplayName("Spring MVC 요청 처리 흐름 테스트")
    void testMvcRequestProcessingFlow() throws Exception {
        Long userId = 789L;
        User mockUser = User.builder().id(userId).name("Flow Test User").build();
        when(helloService.getUserById(userId)).thenReturn(mockUser);
        mockMvc.perform(get("/world/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("world"))
                .andExpect(model().attributeExists("user"))
                .andDo(result -> {
                    log.info("📋 MVC 요청 처리 흐름:");
                    log.info("  1. DispatcherServlet -> 요청 수신");
                    log.info("  2. HandlerMapping -> Controller 매핑");
                    log.info("  3. Controller -> 비즈니스 로직 처리");
                    log.info("  4. ModelAndView -> 뷰와 모델 데이터 생성");
                    log.info("  5. ViewResolver -> 뷰 해석");
                    log.info("  6. View -> 최종 응답 렌더링");
                })
                .andDo(print());

        log.info("✅ Spring MVC 요청 처리 흐름 테스트 완료");
    }
}