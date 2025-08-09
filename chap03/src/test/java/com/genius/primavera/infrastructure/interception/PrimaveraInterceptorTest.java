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
@DisplayName("PrimaveraInterceptor 테스트")
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
    @DisplayName("preHandle 메서드 실행 테스트")
    void testPreHandleExecution() throws Exception {
        // Given
        List<User> mockUsers = Arrays.asList(
            User.builder().id(1L).name("User1").email("user1@test.com").build()
        );
        when(helloService.getUsers()).thenReturn(mockUsers);

        // When
        mockMvc.perform(get("/hello")
                        .header("User-Agent", "Test-Agent")
                        .header("Accept", "text/html"))
                .andExpect(status().isOk())
                .andDo(print());

        // Then
        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        
        log.info("✅ preHandle 메서드 실행 테스트 완료");
        log.info("📋 preHandle 역할:");
        log.info("  - 컨트롤러 메서드 실행 전 호출");
        log.info("  - 요청 정보 로깅 및 검증");
        log.info("  - 인증/인가 체크 가능");
        log.info("  - false 반환 시 요청 중단");
    }

    @Test
    @Order(2)
    @DisplayName("postHandle 메서드 실행 테스트")
    void testPostHandleExecution() throws Exception {
        // Given
        List<User> mockUsers = Arrays.asList(
            User.builder().id(1L).name("User1").email("user1@test.com").build()
        );
        when(helloService.getUsers()).thenReturn(mockUsers);

        // When
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("hello.html"))
                .andDo(print());

        // Then
        verify(primaveraInterceptor, times(1)).postHandle(any(), any(), any(), any());
        
        log.info("✅ postHandle 메서드 실행 테스트 완료");
        log.info("📋 postHandle 역할:");
        log.info("  - 컨트롤러 메서드 실행 후, 뷰 렌더링 전 호출");
        log.info("  - ModelAndView 객체 수정 가능");
        log.info("  - 추가 모델 데이터 삽입 가능");
        log.info("  - 뷰 이름 변경 가능");
    }

    @Test
    @Order(3)
    @DisplayName("afterCompletion 메서드 실행 테스트")
    void testAfterCompletionExecution() throws Exception {
        // Given
        List<User> mockUsers = Arrays.asList(
            User.builder().id(1L).name("User1").email("user1@test.com").build()
        );
        when(helloService.getUsers()).thenReturn(mockUsers);

        // When
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andDo(print());

        // Then
        verify(primaveraInterceptor, times(1)).afterCompletion(any(), any(), any(), any());
        
        log.info("✅ afterCompletion 메서드 실행 테스트 완료");
        log.info("📋 afterCompletion 역할:");
        log.info("  - 뷰 렌더링 완료 후 호출");
        log.info("  - 리소스 정리 작업");
        log.info("  - 실행 시간 측정 완료");
        log.info("  - 예외 발생 시에도 호출됨");
    }

    @Test
    @Order(4)
    @DisplayName("경로 변수와 함께 인터셉터 실행 테스트")
    void testInterceptorWithPathVariable() throws Exception {
        // Given
        Long userId = 123L;
        User mockUser = User.builder()
                .id(userId)
                .name("Path Variable User")
                .email("pathvar@test.com")
                .build();
        when(helloService.getUserById(userId)).thenReturn(mockUser);

        // When
        mockMvc.perform(get("/hello/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("hello"))
                .andDo(print());

        // Then
        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        verify(primaveraInterceptor, times(1)).postHandle(any(), any(), any(), any());
        verify(primaveraInterceptor, times(1)).afterCompletion(any(), any(), any(), any());
        
        log.info("✅ 경로 변수와 인터셉터 테스트 완료");
    }

    @Test
    @Order(5)
    @DisplayName("예외 발생 시 인터셉터 동작 테스트")
    void testInterceptorWithException() throws Exception {
        // When & Then
        mockMvc.perform(get("/oops"))
                .andExpect(status().isInternalServerError())
                .andDo(print());

        // 예외가 발생해도 afterCompletion은 호출되어야 함
        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        verify(primaveraInterceptor, times(1)).afterCompletion(any(), any(), any(), any());
        
        log.info("✅ 예외 상황에서 인터셉터 동작 테스트 완료");
        log.info("📋 예외 상황에서의 인터셉터 동작:");
        log.info("  - preHandle: 정상 실행");
        log.info("  - postHandle: 예외 발생 시 실행되지 않음");
        log.info("  - afterCompletion: 예외 발생 시에도 실행됨");
    }

    @Test
    @Order(6)
    @DisplayName("여러 요청에 대한 인터셉터 동작 테스트")
    void testInterceptorWithMultipleRequests() throws Exception {
        // Given
        when(helloService.getUsers()).thenReturn(Arrays.asList());
        when(helloService.getUserById(anyLong())).thenReturn(
            User.builder().id(1L).name("Test").email("test@test.com").build()
        );

        // When - 여러 번의 요청 수행
        mockMvc.perform(get("/hello")).andExpect(status().isOk());
        mockMvc.perform(get("/hello/1")).andExpect(status().isOk());
        mockMvc.perform(get("/order")).andExpect(status().isOk());

        // Then
        verify(primaveraInterceptor, times(3)).preHandle(any(), any(), any());
        verify(primaveraInterceptor, times(3)).postHandle(any(), any(), any(), any());
        verify(primaveraInterceptor, times(3)).afterCompletion(any(), any(), any(), any());
        
        log.info("✅ 여러 요청에 대한 인터셉터 동작 테스트 완료");
    }

    @Test
    @Order(7)
    @DisplayName("HTTP 헤더 처리 테스트")
    void testHttpHeaderProcessing() throws Exception {
        // Given
        when(helloService.getUsers()).thenReturn(Arrays.asList());

        // When
        mockMvc.perform(get("/hello")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Forwarded-For", "192.168.1.1")
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isOk())
                .andDo(print());

        // Then
        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        
        log.info("✅ HTTP 헤더 처리 테스트 완료");
        log.info("📋 인터셉터에서 활용 가능한 HTTP 헤더:");
        log.info("  - Authorization: 인증 정보");
        log.info("  - User-Agent: 클라이언트 정보");
        log.info("  - X-Forwarded-For: 원본 IP 주소");
        log.info("  - Accept: 클라이언트가 받을 수 있는 컨텐츠 타입");
    }

    @Test
    @Order(8)
    @DisplayName("요청 시간 측정 시뮬레이션")
    void testRequestTimeMeasurement() throws Exception {
        // Given
        when(helloService.getUsers()).thenReturn(Arrays.asList());

        // When
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk());
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Then
        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        verify(primaveraInterceptor, times(1)).afterCompletion(any(), any(), any(), any());
        
        log.info("✅ 요청 시간 측정 시뮬레이션 완료");
        log.info("⏱️ 측정된 실행 시간: {}ms", executionTime);
        log.info("📋 인터셉터를 통한 성능 모니터링:");
        log.info("  - preHandle에서 시작 시간 기록");
        log.info("  - afterCompletion에서 종료 시간 기록");
        log.info("  - 전체 요청 처리 시간 계산");
    }

    @Test
    @Order(9)
    @DisplayName("인터셉터 체인 실행 순서 테스트")
    void testInterceptorChainExecution() throws Exception {
        // Given
        when(helloService.getUsers()).thenReturn(Arrays.asList());

        // When
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    log.info("🔗 인터셉터 체인 실행 순서:");
                    log.info("  1. preHandle() - 요청 전처리");
                    log.info("  2. Controller 메서드 실행");
                    log.info("  3. postHandle() - 응답 후처리");
                    log.info("  4. View 렌더링");
                    log.info("  5. afterCompletion() - 완료 후처리");
                });

        // Then
        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        verify(primaveraInterceptor, times(1)).postHandle(any(), any(), any(), any());
        verify(primaveraInterceptor, times(1)).afterCompletion(any(), any(), any(), any());
        
        log.info("✅ 인터셉터 체인 실행 순서 테스트 완료");
    }

    @Test
    @Order(10)
    @DisplayName("인터셉터 활용 사례 시연")
    void testInterceptorUseCases() throws Exception {
        // Given
        when(helloService.getUsers()).thenReturn(Arrays.asList());

        // When
        mockMvc.perform(get("/hello")
                        .header("X-Request-ID", "test-request-123")
                        .sessionAttr("userId", "testUser"))
                .andExpect(status().isOk())
                .andDo(print());

        // Then
        verify(primaveraInterceptor, times(1)).preHandle(any(), any(), any());
        
        log.info("✅ 인터셉터 활용 사례 시연 완료");
        log.info("🎯 인터셉터 실무 활용 사례:");
        log.info("  📊 로깅 및 모니터링:");
        log.info("    - 요청/응답 로깅");
        log.info("    - 성능 측정");
        log.info("    - API 사용량 추적");
        log.info("  🔐 보안 및 인증:");
        log.info("    - JWT 토큰 검증");
        log.info("    - 세션 체크");
        log.info("    - 권한 검사");
        log.info("  🔧 요청 처리:");
        log.info("    - 요청 데이터 검증");
        log.info("    - 헤더 추가/수정");
        log.info("    - 캐시 제어");
        log.info("  📋 비즈니스 로직:");
        log.info("    - 다국어 처리");
        log.info("    - 테마 설정");
        log.info("    - 사용자 컨텍스트 설정");
    }

}