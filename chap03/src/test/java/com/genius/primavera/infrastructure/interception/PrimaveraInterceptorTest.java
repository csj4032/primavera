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
    @DisplayName("preHandle translated_text_3 execution test")
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
        
        log.info(" preHandle translated_text_3 execution test completed");
        log.info(" preHandle translated_text_2:");
        log.info("  - controller translated_text_3 execution translated_text_1 called");
        log.info("  - translated_text_2 information translated_text_2 translated_text_1 validation");
        log.info("  - translated_text_2/translated_text_2 translated_text_2 translated_text_2");
        log.info("  - false translated_text_2 translated_text_1 translated_text_2 translated_text_2");
    }

    @Test
    @Order(2)
    @DisplayName("postHandle translated_text_3 execution test")
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
        
        log.info(" postHandle translated_text_3 execution test completed");
        log.info(" postHandle translated_text_2:");
        log.info("  - controller translated_text_3 execution translated_text_1, translated_text_1 translated_text_3 translated_text_1 called");
        log.info("  - ModelAndView translated_text_2 modification translated_text_2");
        log.info("  - translated_text_2 translated_text_2 data translated_text_2 translated_text_2");
        log.info("  - translated_text_1 translated_text_2 translated_text_2 translated_text_2");
    }

    @Test
    @Order(3)
    @DisplayName("afterCompletion translated_text_3 execution test")
    void testAfterCompletionExecution() throws Exception {

        List<User> mockUsers = Arrays.asList(
            User.builder().id(1L).name("User1").email("user1@test.com").build()
        );
        when(helloService.getUsers()).thenReturn(mockUsers);

        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andDo(print());

        verify(primaveraInterceptor, times(1)).afterCompletion(any(), any(), any(), any());
        
        log.info(" afterCompletion translated_text_3 execution test completed");
        log.info(" afterCompletion translated_text_2:");
        log.info("  - translated_text_1 translated_text_3 completed translated_text_1 called");
        log.info("  - translated_text_3 translated_text_2 translated_text_2");
        log.info("  - execution translated_text_1 translated_text_2 completed");
        log.info("  - exception translated_text_2 translated_text_1 calledtranslated_text_1");
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_2 translated_text_3 translated_text_2 translated_text_4 execution test")
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
        
        log.info(" translated_text_2 translated_text_3 translated_text_4 test completed");
    }

    @Test
    @Order(5)
    @DisplayName("exception translated_text_2 translated_text_1 translated_text_4 translated_text_2 test")
    void testInterceptorWithException() throws Exception {

        mockMvc.perform(get("/oops"))
                .andExpect(status().isInternalServerError())
                .andDo(print());

        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        verify(primaveraInterceptor, times(1)).afterCompletion(any(), any(), any(), any());
        
        log.info(" exception translated_text_4 translated_text_4 translated_text_2 test completed");
        log.info(" exception translated_text_4 translated_text_4 translated_text_2:");
        log.info("  - preHandle: translated_text_2 execution");
        log.info("  - postHandle: exception translated_text_2 translated_text_1 execution translated_text_2");
        log.info("  - afterCompletion: exception translated_text_2 translated_text_1 executiontranslated_text_1");
    }

    @Test
    @Order(6)
    @DisplayName("translated_text_2 translated_text_2 translated_text_2 translated_text_4 translated_text_2 test")
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
        
        log.info(" translated_text_2 translated_text_2 translated_text_2 translated_text_4 translated_text_2 test completed");
    }

    @Test
    @Order(7)
    @DisplayName("HTTP translated_text_2 processing test")
    void testHttpHeaderProcessing() throws Exception {

        when(helloService.getUsers()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/hello")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Forwarded-For", "192.168.1.1")
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isOk())
                .andDo(print());

        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        
        log.info(" HTTP translated_text_2 processing test completed");
        log.info(" translated_text_4translated_text_2 translated_text_2 translated_text_2 HTTP translated_text_2:");
        log.info("  - Authorization: translated_text_2 information");
        log.info("  - User-Agent: translated_text_5 information");
        log.info("  - X-Forwarded-For: translated_text_2 IP translated_text_2");
        log.info("  - Accept: translated_text_5 translated_text_2 translated_text_1 translated_text_2 translated_text_3 translated_text_2");
    }

    @Test
    @Order(8)
    @DisplayName("translated_text_2 translated_text_1 translated_text_2 translated_text_1")
    void testRequestTimeMeasurement() throws Exception {

        when(helloService.getUsers()).thenReturn(Arrays.asList());

        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk());
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        verify(primaveraInterceptor, times(1)).afterCompletion(any(), any(), any(), any());
        
        log.info(" translated_text_2 translated_text_1 translated_text_2 translated_text_1 completed");
        log.info("⏱ translated_text_2 execution translated_text_1: {}ms", executionTime);
        log.info(" translated_text_4 translated_text_2 translated_text_2 translated_text_4:");
        log.info("  - preHandletranslated_text_2 translated_text_1 translated_text_1 translated_text_2");
        log.info("  - afterCompletiontranslated_text_2 translated_text_2 translated_text_1 translated_text_2");
        log.info("  - translated_text_1 translated_text_2 processing translated_text_1 translated_text_2");
    }

    @Test
    @Order(9)
    @DisplayName("translated_text_4 translated_text_2 execution translated_text_2 test")
    void testInterceptorChainExecution() throws Exception {

        when(helloService.getUsers()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    log.info(" translated_text_4 translated_text_2 execution translated_text_2:");
                    log.info("  1. preHandle() - translated_text_2 translated_text_1processing");
                    log.info("  2. Controller translated_text_3 execution");
                    log.info("  3. postHandle() - translated_text_2 translated_text_1processing");
                    log.info("  4. View translated_text_3");
                    log.info("  5. afterCompletion() - completed translated_text_1processing");
                });

        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        verify(primaveraInterceptor, times(1)).postHandle(any(), any(), any(), any());
        verify(primaveraInterceptor, times(1)).afterCompletion(any(), any(), any(), any());
        
        log.info(" translated_text_4 translated_text_2 execution translated_text_2 test completed");
    }

    @Test
    @Order(10)
    @DisplayName("translated_text_4 translated_text_2 translated_text_2 translated_text_1")
    void testInterceptorUseCases() throws Exception {

        when(helloService.getUsers()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/hello")
                        .header("X-Request-ID", "test-request-123")
                        .sessionAttr("userId", "testUser"))
                .andExpect(status().isOk())
                .andDo(print());

        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        
        log.info(" translated_text_4 translated_text_2 translated_text_2 translated_text_1 completed");
        log.info(" translated_text_4 translated_text_2 translated_text_2 translated_text_2:");
        log.info("   translated_text_2 translated_text_1 translated_text_4:");
        log.info("    - translated_text_2/translated_text_2 translated_text_2");
        log.info("    - translated_text_2 translated_text_2");
        log.info("    - API translated_text_3 translated_text_2");
        log.info("   translated_text_2 translated_text_1 translated_text_2:");
        log.info("    - JWT translated_text_2 validation");
        log.info("    - translated_text_2 translated_text_2");
        log.info("    - translated_text_2 translated_text_2");
        log.info("   translated_text_2 processing:");
        log.info("    - translated_text_2 data validation");
        log.info("    - translated_text_2 translated_text_2/modification");
        log.info("    - translated_text_1 translated_text_2");
        log.info("   translated_text_4 translated_text_2:");
        log.info("    - translated_text_3 processing");
        log.info("    - translated_text_2 translated_text_2");
        log.info("    - user translated_text_4 translated_text_2");
    }

}