package com.genius.primavera.infrastructure.interception;

import com.genius.primavera.applicaiton.HelloService;
import com.genius.primavera.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("PrimaveraInterceptor test")
class PrimaveraInterceptorTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private HelloService helloService;

    @SpyBean
    private PrimaveraInterceptor primaveraInterceptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @Order(1)
    @DisplayName("preHandle connection execution test")
    void testPreHandleExecution() throws Exception {

        List<User> mockUsers = Arrays.asList(
            User.builder().id(1L).name("User1").email("user1@test.com").build()
        );
        when(helloService.getUsers()).thenReturn(mockUsers);

        mockMvc.perform(get("/hello")
                        .header("User-Agent", "Test-Agent")
                        .header("Accept", "text/html"))
                .andExpect(status().isOk())
                .andDo(print());

        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        
        log.info(" preHandle connection execution test completed");
        log.info(" preHandle test:");
        log.info("  - controller connection execution should called");
        log.info("  - test information test should validation");
        log.info("  - test/test test");
        log.info("  - false test should test");
    }

    @Test
    @Order(2)
    @DisplayName("postHandle connection execution test")
    void testPostHandleExecution() throws Exception {

        List<User> mockUsers = Arrays.asList(
            User.builder().id(1L).name("User1").email("user1@test.com").build()
        );
        when(helloService.getUsers()).thenReturn(mockUsers);

        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("hello.html"))
                .andDo(print());

        verify(primaveraInterceptor, times(1)).postHandle(any(), any(), any(), any());
        
        log.info(" postHandle connection execution test completed");
        log.info(" postHandle test:");
        log.info("  - controller connection execution should, should connection should called");
        log.info("  - ModelAndView test modification test");
        log.info("  - test data test");
        log.info("  - should test test");
    }

    @Test
    @Order(3)
    @DisplayName("afterCompletion connection execution test")
    void testAfterCompletionExecution() throws Exception {

        List<User> mockUsers = Arrays.asList(
            User.builder().id(1L).name("User1").email("user1@test.com").build()
        );
        when(helloService.getUsers()).thenReturn(mockUsers);

        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andDo(print());

        verify(primaveraInterceptor, times(1)).afterCompletion(any(), any(), any(), any());
        
        log.info(" afterCompletion connection execution test completed");
        log.info(" afterCompletion test:");
        log.info("  - should connection completed should called");
        log.info("  - connection test");
        log.info("  - execution should test completed");
        log.info("  - exception test should calledshould");
    }

    @Test
    @Order(4)
    @DisplayName("test connection test file execution test")
    void testInterceptorWithPathVariable() throws Exception {

        Long userId = 123L;
        User mockUser = User.builder()
                .id(userId)
                .name("Path Variable User")
                .email("pathvar@test.com")
                .build();
        when(helloService.getUserById(userId)).thenReturn(mockUser);

        mockMvc.perform(get("/hello/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("hello"))
                .andDo(print());

        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        verify(primaveraInterceptor, times(1)).postHandle(any(), any(), any(), any());
        verify(primaveraInterceptor, times(1)).afterCompletion(any(), any(), any(), any());
        
        log.info(" test connection file test completed");
    }

    @Test
    @Order(5)
    @DisplayName("exception test should file test")
    void testInterceptorWithException() throws Exception {

        mockMvc.perform(get("/oops"))
                .andExpect(status().isInternalServerError())
                .andDo(print());

        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        verify(primaveraInterceptor, times(1)).afterCompletion(any(), any(), any(), any());
        
        log.info(" exception file test completed");
        log.info(" exception file test:");
        log.info("  - preHandle: test execution");
        log.info("  - postHandle: exception test should execution test");
        log.info("  - afterCompletion: exception test should executionshould");
    }

    @Test
    @Order(6)
    @DisplayName("test test file test")
    void testInterceptorWithMultipleRequests() throws Exception {

        when(helloService.getUsers()).thenReturn(Arrays.asList());
        when(helloService.getUserById(anyLong())).thenReturn(
            User.builder().id(1L).name("Test").email("test@test.com").build()
        );

        mockMvc.perform(get("/hello")).andExpect(status().isOk());
        mockMvc.perform(get("/hello/1")).andExpect(status().isOk());
        mockMvc.perform(get("/order")).andExpect(status().isOk());

        verify(primaveraInterceptor, times(3)).preHandle(any(), any(), any());
        verify(primaveraInterceptor, times(3)).postHandle(any(), any(), any(), any());
        verify(primaveraInterceptor, times(3)).afterCompletion(any(), any(), any(), any());
        
        log.info(" test test file test completed");
    }

    @Test
    @Order(7)
    @DisplayName("HTTP test processing test")
    void testHttpHeaderProcessing() throws Exception {

        when(helloService.getUsers()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/hello")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Forwarded-For", "192.168.1.1")
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isOk())
                .andDo(print());

        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        
        log.info(" HTTP test processing test completed");
        log.info(" filetest test HTTP test:");
        log.info("  - Authorization: test information");
        log.info("  - User-Agent: Endpoint information");
        log.info("  - X-Forwarded-For: test IP test");
        log.info("  - Accept: processing test should test connection test");
    }

    @Test
    @Order(8)
    @DisplayName("test should")
    void testRequestTimeMeasurement() throws Exception {

        when(helloService.getUsers()).thenReturn(Arrays.asList());

        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk());
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        verify(primaveraInterceptor, times(1)).afterCompletion(any(), any(), any(), any());
        
        log.info(" test should completed");
        log.info("⏱ test execution should: {}ms", executionTime);
        log.info(" file test file:");
        log.info("  - preHandletest needs to be added test");
        log.info("  - afterCompletiontest should test");
        log.info("  - should test processing should test");
    }

    @Test
    @Order(9)
    @DisplayName("file test execution test")
    void testInterceptorChainExecution() throws Exception {

        when(helloService.getUsers()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    log.info(" file test execution test:");
                    log.info("  1. preHandle() - test shouldprocessing");
                    log.info("  2. Controller connection execution");
                    log.info("  3. postHandle() - test shouldprocessing");
                    log.info("  4. View connection");
                    log.info("  5. afterCompletion() - completed shouldprocessing");
                });

        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        verify(primaveraInterceptor, times(1)).postHandle(any(), any(), any(), any());
        verify(primaveraInterceptor, times(1)).afterCompletion(any(), any(), any(), any());
        
        log.info(" file test execution test completed");
    }

    @Test
    @Order(10)
    @DisplayName("file test should")
    void testInterceptorUseCases() throws Exception {

        when(helloService.getUsers()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/hello")
                        .header("X-Request-ID", "test-request-123")
                        .sessionAttr("userId", "testUser"))
                .andExpect(status().isOk())
                .andDo(print());

        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        
        log.info(" file test should completed");
        log.info(" file test test:");
        log.info("   test should file:");
        log.info("    - test/test");
        log.info("    - test");
        log.info("    - API connection test");
        log.info("   test should test:");
        log.info("    - JWT test validation");
        log.info("    - test");
        log.info("    - test");
        log.info("   test processing:");
        log.info("    - test data validation");
        log.info("    - test/modification");
        log.info("    - should test");
        log.info("   file test:");
        log.info("    - connection processing");
        log.info("    - test");
        log.info("    - user file test");
    }

}