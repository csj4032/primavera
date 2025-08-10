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
@DisplayName("Spring MVC file test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SpringMvcArchitectureTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private HelloService helloService;

    private MockMvc mockMvc;

    @Test
    @Order(1)
    @DisplayName("DispatcherServlet test should test")
    void testDispatcherServletConfiguration() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Map<String, DispatcherServlet> dispatcherServlets = webApplicationContext.getBeansOfType(DispatcherServlet.class);
        assertThat(dispatcherServlets).isNotEmpty();
        log.info(" DispatcherServlet test verification: {}", dispatcherServlets.keySet());
        log.info(" DispatcherServlet test:");
        log.info("  - HTTP connection test connection (Front Controller test)");
        log.info("  - connection Handler(Controller) test");
        log.info("  - View Resolution processing");
        log.info("  - exception processing should test creation");
    }

    @Test
    @Order(2)
    @DisplayName("HandlerMapping test")
    void testHandlerMapping() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        List<User> mockUsers = Arrays.asList(User.builder().id(1L).name("User1").email("user1@test.com").build());
        when(helloService.getUsers()).thenReturn(mockUsers);
        MvcResult result = mockMvc.perform(get("/hello")).andExpect(status().isOk()).andReturn();
        Object handler = result.getHandler();
        assertThat(handler).isNotNull();
        log.info(" HandlerMapping result:");
        log.info("  - connection Handler: {}", handler.getClass().getSimpleName());
        log.info("  - test URL '/hello'should HelloController.helloWorld()should connection");
        log.info(" HandlerMapping test:");
        log.info("  - RequestMappingHandlerMapping: @RequestMapping annotation test");
        log.info("  - BeanNameUrlHandlerMapping: Bean connection URL test");
        log.info("  - SimpleUrlHandlerMapping: test URL test");
    }

    @Test
    @Order(3)
    @DisplayName("Controller-Service-Repository test test")
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
                    log.info(" test test:");
                    log.info("  1. Controller test - HTTP test/test processing");
                    log.info("      HelloController.helloWorldById()");
                    log.info("  2. Service test - file test processing");
                    log.info("      HelloService.getUserById()");
                    log.info("  3. Repository test - data test (connection Mock)");
                    log.info("      data Endpoint file");
                })
                .andDo(print());

        log.info(" test test completed");
    }

    @Test
    @Order(4)
    @DisplayName("ViewResolver test")
    void testViewResolver() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(helloService.getUsers()).thenReturn(Arrays.asList());
        MvcResult result = mockMvc.perform(get("/hello")).andExpect(status().isOk()).andReturn();
        String viewName = result.getModelAndView().getViewName();
        assertThat(viewName).isEqualTo("hello");
        Map<String, ViewResolver> viewResolvers = webApplicationContext.getBeansOfType(ViewResolver.class);
        assertThat(viewResolvers).isNotEmpty();
        log.info(" ViewResolver test:");
        log.info("  - connection should test: '{}'", viewName);
        log.info("  - created successfully ViewResolver should: {}", viewResolvers.size());
        log.info("  - ViewResolver test: {}", viewResolvers.keySet());
        log.info(" ViewResolver test:");
        log.info("  - connection should test should file test");
        log.info("  - connection test(Thymeleaf, JSP should)should test");
        log.info("  - test should test connection");
    }

    @Test
    @Order(5)
    @DisplayName("Modelshould View data connection test")
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

        log.info(" Model-View data connection:");
        log.info("  - Model test should: {}", model.size());
        log.info("  - 'hello' test: {} test User test", modelUsers.size());
        log.info("  - should test: '{}'", result.getModelAndView().getViewName());

        log.info(" data connection file:");
        log.info("  1. Controllershould Modelshould data should");
        log.info("  2. ModelAndView test creation");
        log.info("  3. ViewResolvershould View test");
        log.info("  4. Viewshould Model data connection");

        log.info(" Model-View data connection test completed");
    }

    @Test
    @Order(6)
    @DisplayName("HTTP file test")
    void testHttpMethodMapping() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(helloService.getUsers()).thenReturn(List.of());
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("hello"))
                .andDo(result -> {
                    log.info(" HTTP connection test:");
                    log.info("  - GET /hello -> HelloController.helloWorld()");
                    log.info("  - @GetMapping annotation test");
                });

        log.info(" HTTP connection test:");
        log.info("  - @GetMapping: data inquiry");
        log.info("  - @PostMapping: data creation");
        log.info("  - @PutMapping: data test should");
        log.info("  - @PatchMapping: data test should");
        log.info("  - @DeleteMapping: data deletion");

        log.info(" HTTP connection test completed");
    }

    @Test
    @Order(7)
    @DisplayName("MVC test test validation")
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
        log.info(" MVC test test:");
        log.info("   Model (data):");
        log.info("    - file data: User test");
        log.info("    - shouldshould connection data test");
        log.info("    - data connection file test");
        log.info("   View (test):");
        log.info("    - test Endpoint");
        log.info("    - connection: {}.html", viewName);
        log.info("    - Model data processing test");
        log.info("   Controller (test):");
        log.info("    - test processing");
        log.info("    - file test called");
        log.info("    - Modelshould View test");
        log.info(" MVC test test validation completed");
    }

    @Test
    @Order(8)
    @DisplayName("Spring MVC test should test validation")
    void testSpringMvcConfiguration() {
        String[] beanNames = webApplicationContext.getBeanDefinitionNames();
        long mvcBeanCount = Arrays.stream(beanNames)
                .filter(name -> name.toLowerCase().contains("mvc") ||
                        name.toLowerCase().contains("handler") ||
                        name.toLowerCase().contains("resolver"))
                .count();

        assertThat(mvcBeanCount).isGreaterThan(0);
        log.info(" Spring MVC test validation:");
        log.info("  - test Bean should: {}", beanNames.length);
        log.info("  - MVC test Bean should: {}", mvcBeanCount);
        log.info(" test MVC test:");
        log.info("  - DispatcherServlet: test processing");
        log.info("  - HandlerMapping: URLshould Handler test");
        log.info("  - ViewResolver: should test");
        log.info("  - MessageConverter: HTTP connection test");
        log.info("  - ExceptionResolver: exception processing");
        log.info(" Spring MVC test validation completed");
    }
}