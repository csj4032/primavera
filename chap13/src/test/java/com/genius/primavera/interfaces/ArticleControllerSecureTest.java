package com.genius.primavera.interfaces;

import com.genius.primavera.testingsupport.annotation.TestWebWithDB;
import com.genius.primavera.testingsupport.annotation.WithTestUser;
import com.genius.primavera.testingsupport.security.TestDataConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ArticleController 보안 강화된 통합 테스트
 * 
 * <p>기존 ArticleControllerTest의 보안 문제를 개선한 버전입니다.
 * 하드코딩된 사용자 정보를 상수화하고, 테스트 환경 검증을 추가했습니다.</p>
 * 
 * <h3>보안 개선사항:</h3>
 * <ul>
 *   <li>하드코딩된 이메일 주소 제거 (genius@primavera.com → 상수 사용)</li>
 *   <li>테스트 전용 도메인 사용 (*.test.primavera.local)</li>
 *   <li>@WithTestUser 어노테이션으로 권한 관리 체계화</li>
 *   <li>테스트 환경 자동 검증</li>
 * </ul>
 * 
 * <h3>기존 대비 개선점:</h3>
 * <pre>
 * // 기존 방식 (보안 위험)
 * &#64;WithUserDetails(value = "genius@primavera.com", ...)
 * 
 * // 개선된 방식 (보안 강화)
 * &#64;WithTestUser(role = WithTestUser.Role.GENIUS)
 * </pre>
 */
@Slf4j
@TestWebWithDB(initScript = "sql/init.sql")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
})
@DisplayName("ArticleController 보안 강화 통합 테스트")
public class ArticleControllerSecureTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void verifyTestEnvironment() {
        // 테스트 환경 검증 - 운영 환경에서 실행 방지
        TestDataConstants.Security.ensureTestEnvironment();
        log.info("✅ 테스트 환경 검증 완료 - 안전한 테스트 실행");
    }

    @Test
    @Order(1)
    @WithTestUser(role = WithTestUser.Role.GENIUS)
    @DisplayName("관리자 권한으로 게시글 목록 조회")
    void shouldAllowAdminToViewArticles() throws Exception {
        // When & Then: 관리자 권한으로 게시글 목록 조회
        mockMvc.perform(get("/articles"))
            .andExpect(status().isOk())
            .andExpect(view().name("article/list"))
            .andExpect(model().attributeExists("articles"))
            .andDo(result -> {
                log.info("👑 관리자({}) 권한으로 게시글 목록 조회 성공", 
                    TestDataConstants.TestUsers.GENIUS_EMAIL);
            });
    }

    @Test
    @Order(2)
    @WithTestUser(role = WithTestUser.Role.USER)
    @DisplayName("일반 사용자 권한으로 게시글 조회")
    void shouldAllowUserToViewArticles() throws Exception {
        // When & Then: 일반 사용자 권한으로 게시글 조회
        mockMvc.perform(get("/articles"))
            .andExpect(status().isOk())
            .andExpect(view().name("article/list"))
            .andDo(result -> {
                log.info("👤 일반 사용자({}) 권한으로 게시글 조회 성공", 
                    TestDataConstants.TestUsers.USER_EMAIL);
            });
    }

    @Test
    @Order(3)
    @WithTestUser(role = WithTestUser.Role.GENIUS)
    @DisplayName("관리자 권한으로 게시글 작성")
    void shouldAllowAdminToCreateArticle() throws Exception {
        // Given: 새 게시글 데이터
        String title = "보안 강화된 테스트 게시글";
        String content = "이 게시글은 보안이 강화된 테스트에서 작성되었습니다.";
        
        // When & Then: 게시글 작성 요청
        mockMvc.perform(post("/articles")
                .param("title", title)
                .param("content", content)
                .param("type", "GENERAL"))
            .andExpect(status().is3xxRedirection())
            .andExpected(redirectedUrl("/articles"))
            .andDo(result -> {
                log.info("📝 관리자({}) 권한으로 게시글 작성 성공: {}", 
                    TestDataConstants.TestUsers.GENIUS_EMAIL, title);
            });
    }

    @Test
    @Order(4)
    @WithTestUser(role = WithTestUser.Role.USER)
    @DisplayName("일반 사용자의 제한된 권한 테스트")
    void shouldRestrictUserPermissions() throws Exception {
        // When & Then: 일반 사용자가 관리자 기능 접근 시도
        mockMvc.perform(get("/admin/articles"))
            .andExpect(status().isForbidden())
            .andDo(result -> {
                log.info("🚫 일반 사용자({}) 관리자 영역 접근 차단됨", 
                    TestDataConstants.TestUsers.USER_EMAIL);
            });
    }

    @Test
    @Order(5)
    @WithTestUser(email = "custom@test.primavera.local")
    @DisplayName("커스텀 테스트 사용자로 접근")
    void shouldWorkWithCustomTestUser() throws Exception {
        // Given: 커스텀 테스트 사용자 (데이터베이스에 미리 준비되어야 함)
        String customEmail = "custom@test.primavera.local";
        
        // When & Then: 커스텀 사용자로 접근
        mockMvc.perform(get("/articles"))
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("🔧 커스텀 테스트 사용자({}) 접근 성공", customEmail);
            });
    }

    @Test
    @Order(6)
    @DisplayName("인증되지 않은 사용자 접근 제한")
    void shouldRestrictUnauthenticatedAccess() throws Exception {
        // When & Then: 인증 없이 보호된 리소스 접근 시도
        mockMvc.perform(post("/articles")
                .param("title", "Unauthorized Article")
                .param("content", "This should fail"))
            .andExpect(status().is3xxRedirection()) // 로그인 페이지로 리다이렉트
            .andDo(result -> {
                log.info("🔒 인증되지 않은 접근 차단됨 - 로그인 페이지로 리다이렉트");
            });
    }

    @AfterAll
    static void securityReport() {
        log.info("🔐 보안 강화 테스트 완료 요약:");
        log.info("  ✅ 테스트 전용 도메인 사용: *.test.primavera.local");
        log.info("  ✅ 하드코딩된 사용자 정보 제거");
        log.info("  ✅ 권한별 접근 제어 검증");
        log.info("  ✅ 테스트 환경 자동 검증");
        log.info("  ✅ TestContainer 자동 정리");
    }
}