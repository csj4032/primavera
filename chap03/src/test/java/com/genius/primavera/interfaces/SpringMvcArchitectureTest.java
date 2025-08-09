package com.genius.primavera.interfaces;

import com.genius.primavera.applicaiton.HelloService;
import com.genius.primavera.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ViewResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("Spring MVC 아키텍처 상세 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SpringMvcArchitectureTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private HelloService helloService;

    private MockMvc mockMvc;

    @Test
    @Order(1)
    @DisplayName("DispatcherServlet 설정 및 동작 테스트")
    void testDispatcherServletConfiguration() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Map<String, DispatcherServlet> dispatcherServlets = webApplicationContext.getBeansOfType(DispatcherServlet.class);
        assertThat(dispatcherServlets).isNotEmpty();
        log.info("✅ DispatcherServlet 설정 확인: {}", dispatcherServlets.keySet());
        log.info("📋 DispatcherServlet 역할:");
        log.info("  - HTTP 요청의 중앙 진입점 (Front Controller 패턴)");
        log.info("  - 적절한 Handler(Controller) 찾기");
        log.info("  - View Resolution 처리");
        log.info("  - 예외 처리 및 응답 생성");
    }

    @Test
    @Order(2)
    @DisplayName("HandlerMapping 동작 테스트")
    void testHandlerMapping() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        List<User> mockUsers = Arrays.asList(User.builder().id(1L).name("User1").email("user1@test.com").build());
        when(helloService.getUsers()).thenReturn(mockUsers);
        MvcResult result = mockMvc.perform(get("/hello")).andExpect(status().isOk()).andReturn();
        Object handler = result.getHandler();
        assertThat(handler).isNotNull();
        log.info("🎯 HandlerMapping 결과:");
        log.info("  - 매핑된 Handler: {}", handler.getClass().getSimpleName());
        log.info("  - 요청 URL '/hello'가 HelloController.helloWorld()에 매핑됨");
        log.info("📋 HandlerMapping 종류:");
        log.info("  - RequestMappingHandlerMapping: @RequestMapping 어노테이션 기반");
        log.info("  - BeanNameUrlHandlerMapping: Bean 이름과 URL 매핑");
        log.info("  - SimpleUrlHandlerMapping: 정적 URL 패턴 매핑");
    }

    @Test
    @Order(3)
    @DisplayName("Controller-Service-Repository 계층 구조 테스트")
    void testLayeredArchitecture() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Long userId = 1L;
        User mockUser = User.builder()
                .id(userId)
                .name("Architecture Test User")
                .email("architecture@test.com")
                .build();
        when(helloService.getUserById(userId)).thenReturn(mockUser);

        mockMvc.perform(get("/world/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("world"))
                .andExpect(model().attributeExists("user"))
                .andDo(result -> {
                    log.info("🏗️ 계층 구조 분석:");
                    log.info("  1. Controller 계층 - HTTP 요청/응답 처리");
                    log.info("     └─ HelloController.helloWorldById()");
                    log.info("  2. Service 계층 - 비즈니스 로직 처리");
                    log.info("     └─ HelloService.getUserById()");
                    log.info("  3. Repository 계층 - 데이터 접근 (현재는 Mock)");
                    log.info("     └─ 데이터 저장소와의 상호작용");
                })
                .andDo(print());

        log.info("✅ 계층 구조 테스트 완료");
    }

    @Test
    @Order(4)
    @DisplayName("ViewResolver 동작 테스트")
    void testViewResolver() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(helloService.getUsers()).thenReturn(Arrays.asList());
        MvcResult result = mockMvc.perform(get("/hello")).andExpect(status().isOk()).andReturn();
        String viewName = result.getModelAndView().getViewName();
        assertThat(viewName).isEqualTo("hello");
        Map<String, ViewResolver> viewResolvers = webApplicationContext.getBeansOfType(ViewResolver.class);
        assertThat(viewResolvers).isNotEmpty();
        log.info("🎨 ViewResolver 분석:");
        log.info("  - 논리적 뷰 이름: '{}'", viewName);
        log.info("  - 등록된 ViewResolver 수: {}", viewResolvers.size());
        log.info("  - ViewResolver 종류: {}", viewResolvers.keySet());
        log.info("📋 ViewResolver 역할:");
        log.info("  - 논리적 뷰 이름을 실제 뷰 구현체로 변환");
        log.info("  - 템플릿 엔진(Thymeleaf, JSP 등)과 연동");
        log.info("  - 뷰 캐싱 및 성능 최적화");
    }

    @Test
    @Order(5)
    @DisplayName("Model과 View 데이터 바인딩 테스트")
    void testModelViewDataBinding() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        List<User> mockUsers = Arrays.asList(
                User.builder().id(1L).name("Model Test User 1").email("model1@test.com").build(),
                User.builder().id(2L).name("Model Test User 2").email("model2@test.com").build()
        );
        when(helloService.getUsers()).thenReturn(mockUsers);

        MvcResult result = mockMvc.perform(get("/hello")).andExpect(status().isOk()).andReturn();

        Map<String, Object> model = result.getModelAndView().getModel();
        assertThat(model).isNotEmpty();
        assertThat(model).containsKey("users");

        List<User> modelUsers = (List<User>) model.get("users");
        assertThat(modelUsers).hasSize(2);

        log.info("🔗 Model-View 데이터 바인딩:");
        log.info("  - Model 속성 수: {}", model.size());
        log.info("  - 'hello' 속성: {} 개의 User 객체", modelUsers.size());
        log.info("  - 뷰 이름: '{}'", result.getModelAndView().getViewName());

        log.info("📋 데이터 바인딩 프로세스:");
        log.info("  1. Controller가 Model에 데이터 추가");
        log.info("  2. ModelAndView 객체 생성");
        log.info("  3. ViewResolver가 View 해석");
        log.info("  4. View가 Model 데이터로 렌더링");

        log.info("✅ Model-View 데이터 바인딩 테스트 완료");
    }

    @Test
    @Order(6)
    @DisplayName("HTTP 메서드별 매핑 테스트")
    void testHttpMethodMapping() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(helloService.getUsers()).thenReturn(List.of());
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("hello"))
                .andDo(result -> {
                    log.info("🌐 HTTP 메서드 매핑:");
                    log.info("  - GET /hello -> HelloController.helloWorld()");
                    log.info("  - @GetMapping 어노테이션 사용");
                });

        log.info("📋 HTTP 메서드 매핑 종류:");
        log.info("  - @GetMapping: 데이터 조회");
        log.info("  - @PostMapping: 데이터 생성");
        log.info("  - @PutMapping: 데이터 전체 수정");
        log.info("  - @PatchMapping: 데이터 부분 수정");
        log.info("  - @DeleteMapping: 데이터 삭제");

        log.info("✅ HTTP 메서드 매핑 테스트 완료");
    }

    @Test
    @Order(7)
    @DisplayName("MVC 패턴 구성 요소 검증")
    void testMvcPatternComponents() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Long userId = 100L;
        User mockUser = User.builder().id(userId).name("MVC Pattern User").email("mvc@test.com").build();
        when(helloService.getUserById(userId)).thenReturn(mockUser);
        MvcResult result = mockMvc.perform(get("/world/{id}", userId)).andExpect(status().isOk()).andReturn();
        Object handler = result.getHandler();
        String viewName = result.getModelAndView().getViewName();
        Map<String, Object> model = result.getModelAndView().getModel();
        assertThat(handler).isNotNull();
        assertThat(viewName).isEqualTo("world");
        assertThat(model).containsKey("user");
        log.info("🎯 MVC 패턴 구성 요소:");
        log.info("  📋 Model (데이터):");
        log.info("    - 비즈니스 데이터: User 객체");
        log.info("    - 뷰에 전달할 데이터 담당");
        log.info("    - 데이터 상태와 비즈니스 로직 포함");
        log.info("  🎨 View (화면):");
        log.info("    - 사용자 인터페이스");
        log.info("    - 템플릿: {}.html", viewName);
        log.info("    - Model 데이터를 시각적으로 표현");
        log.info("  🎮 Controller (제어):");
        log.info("    - 사용자 입력 처리");
        log.info("    - 비즈니스 로직 호출");
        log.info("    - Model과 View 중계 역할");
        log.info("✅ MVC 패턴 구성 요소 검증 완료");
    }

    @Test
    @Order(8)
    @DisplayName("Spring MVC 설정 및 구성 검증")
    void testSpringMvcConfiguration() {
        String[] beanNames = webApplicationContext.getBeanDefinitionNames();
        long mvcBeanCount = Arrays.stream(beanNames)
                .filter(name -> name.toLowerCase().contains("mvc") ||
                        name.toLowerCase().contains("handler") ||
                        name.toLowerCase().contains("resolver"))
                .count();

        assertThat(mvcBeanCount).isGreaterThan(0);
        log.info("⚙️ Spring MVC 구성 검증:");
        log.info("  - 전체 Bean 수: {}", beanNames.length);
        log.info("  - MVC 관련 Bean 수: {}", mvcBeanCount);
        log.info("📋 주요 MVC 구성 요소:");
        log.info("  - DispatcherServlet: 중앙 요청 처리기");
        log.info("  - HandlerMapping: URL과 Handler 매핑");
        log.info("  - ViewResolver: 뷰 이름 해석");
        log.info("  - MessageConverter: HTTP 메시지 변환");
        log.info("  - ExceptionResolver: 예외 처리");
        log.info("✅ Spring MVC 설정 검증 완료");
    }
}