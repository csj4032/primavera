package com.genius.primavera.interfaces;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PostController 통합 테스트 - Mixin 상속 방식 데모
 * 
 * <p>WebIntegrationTest를 상속받아 복잡한 TestContainers 설정을 간소화한 예시입니다.
 * 상속 방식은 JUnit 5의 모든 기능이 정상 작동합니다.</p>
 * 
 * <h3>개선된 점:</h3>
 * <ul>
 *   <li>30줄 이상의 컨테이너 설정 코드 제거</li>
 *   <li>MockMvc 자동 구성</li>
 *   <li>MariaDB TestContainer 자동 관리</li>
 *   <li>JUnit 5 라이프사이클 완전 지원</li>
 * </ul>
 * 
 * <h3>사용된 기술:</h3>
 * <ul>
 *   <li>WebIntegrationTest 상속 - 웹 + DB 통합 환경</li>
 *   <li>MockMvc - HTTP 요청/응답 테스트</li>
 *   <li>MariaDB 11.4.7 TestContainer</li>
 * </ul>
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableAutoConfiguration(exclude = {
    // OAuth2 설정 제외 (chap11 특화 설정)
    org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
})
@DisplayName("PostController 통합 테스트 - Mixin 상속 방식")
public class PostControllerMigrationTest extends WebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    @DisplayName("게시글 목록 조회 - GET /posts")
    void shouldReturnPostList() throws Exception {
        // Given: MariaDB가 init.sql로 초기화된 상태
        
        // When & Then: 게시글 목록 API 호출
        mockMvc.perform(get("/posts"))
            .andExpect(status().isOk())
            .andExpect(view().name("posting/list"))
            .andExpect(model().attributeExists("posts"))
            .andDo(result -> {
                log.info("📋 게시글 목록 조회 성공: {}", 
                    result.getResponse().getContentAsString());
            });
    }

    @Test
    @Order(2) 
    @DisplayName("게시글 작성 폼 - GET /posts/new")
    void shouldShowPostForm() throws Exception {
        // When & Then: 게시글 작성 폼 조회
        mockMvc.perform(get("/posts/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("posting/form"))
            .andExpect(model().attributeExists("post"))
            .andDo(result -> {
                log.info("📝 게시글 작성 폼 로드 성공");
            });
    }

    @Test
    @Order(3)
    @DisplayName("존재하지 않는 게시글 조회 시 404 응답")
    void shouldReturn404ForNonExistentPost() throws Exception {
        // Given: 존재하지 않는 게시글 ID
        long nonExistentId = 999999L;
        
        // When & Then: 404 응답 확인
        mockMvc.perform(get("/posts/{id}", nonExistentId))
            .andExpect(status().isNotFound())
            .andDo(result -> {
                log.info("🚫 존재하지 않는 게시글 요청 시 404 응답 확인됨");
            });
    }
    
    /**
     * 테스트 완료 후 정리 작업
     * TestContainer는 자동으로 정리되므로 별도 작업 불필요
     */
    @AfterAll
    static void tearDown() {
        log.info("✅ PostController 통합 테스트 완료 - TestContainer 자동 정리됨");
    }
}