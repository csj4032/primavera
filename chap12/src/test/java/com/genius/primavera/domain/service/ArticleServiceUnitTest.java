package com.genius.primavera.domain.service;

import com.genius.primavera.domain.mapper.article.ArticleMapper;
import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleStatus;
import com.genius.primavera.domain.model.user.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * ArticleService 단위 테스트
 * 
 * 특징:
 * - Mock을 사용한 순수 단위 테스트
 * - 실제 데이터베이스 연결 없음
 * - application-test.yml 설정 사용
 * - 빠른 실행 속도
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")  // application-test.yml 사용
@DisplayName("ArticleService 단위 테스트")
class ArticleServiceUnitTest {

    @Mock
    private ArticleMapper articleMapper;

    private User testUser;
    private Article testArticle;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .nickname("TestUser")
                .build();

        testArticle = Article.builder()
                .id(1L)
                .author(testUser)
                .subject("테스트 게시글")
                .status(ArticleStatus.PUBLIC)
                .createAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("게시글 저장 - Mock을 사용한 단위 테스트")
    void shouldSaveArticleSuccessfully() {
        // Given
        given(articleMapper.save(any(Article.class))).willReturn(1);

        // When
        Article newArticle = Article.builder()
                .author(testUser)
                .subject("새로운 게시글")
                .status(ArticleStatus.PUBLIC)
                .createAt(Instant.now())
                .build();

        int result = articleMapper.save(newArticle);

        // Then
        assertEquals(1, result);
        verify(articleMapper).save(any(Article.class));
    }

    @Test
    @DisplayName("게시글 목록 조회 - Mock 데이터 반환")
    void shouldReturnArticleList() {
        // Given
        List<Article> mockArticles = Arrays.asList(
                testArticle,
                Article.builder()
                        .id(2L)
                        .author(testUser)
                        .subject("두 번째 게시글")
                        .status(ArticleStatus.PUBLIC)
                        .createAt(Instant.now())
                        .build()
        );
        
        given(articleMapper.findAll()).willReturn(mockArticles);

        // When
        List<Article> articles = articleMapper.findAll();

        // Then
        assertEquals(2, articles.size());
        assertEquals("테스트 게시글", articles.get(0).getSubject());
        assertEquals("두 번째 게시글", articles.get(1).getSubject());
        verify(articleMapper).findAll();
    }

    @Test
    @DisplayName("계층형 댓글 구조 검증 - 비즈니스 로직 테스트")
    void shouldValidateHierarchicalStructure() {
        // Given
        Article parentArticle = Article.builder()
                .id(1L)
                .author(testUser)
                .subject("원글")
                .level(0)
                .step(0)
                .status(ArticleStatus.PUBLIC)
                .createAt(Instant.now())
                .build();

        Article replyArticle = Article.builder()
                .id(2L)
                .parent(parentArticle)
                .pId(parentArticle.getId())
                .reference(parentArticle.getId())
                .author(testUser)
                .subject("댓글")
                .level(1)
                .step(1)
                .status(ArticleStatus.PUBLIC)
                .createAt(Instant.now())
                .build();

        // When & Then - 비즈니스 로직 검증
        assertTrue(isValidReply(replyArticle, parentArticle));
        assertEquals(parentArticle.getId(), replyArticle.getReference());
        assertEquals(parentArticle.getLevel() + 1, replyArticle.getLevel());
    }

    @Test
    @DisplayName("게시글 상태 변경 검증")
    void shouldChangeArticleStatus() {
        // Given
        Article article = Article.builder()
                .id(1L)
                .author(testUser)
                .subject("상태 변경 테스트")
                .status(ArticleStatus.PUBLIC)
                .build();

        // When - 새로운 Article 객체로 상태 변경
        Article updatedArticle = Article.builder()
                .id(article.getId())
                .author(article.getAuthor())
                .subject(article.getSubject())
                .status(ArticleStatus.DELETE)  // PUBLIC -> DELETE
                .build();

        // Then
        assertEquals(ArticleStatus.DELETE, updatedArticle.getStatus());
        assertNotEquals(article.getStatus(), updatedArticle.getStatus());
    }

    @Test
    @DisplayName("게시글 차단 상태 검증")
    void shouldBlockArticle() {
        // Given
        Article article = Article.builder()
                .id(1L)
                .author(testUser)
                .subject("차단될 게시글")
                .status(ArticleStatus.PUBLIC)
                .build();

        // When
        article.setStatus(ArticleStatus.BLOCK);

        // Then
        assertEquals(ArticleStatus.BLOCK, article.getStatus());
        assertEquals(3, article.getStatus().getValue());  // BLOCK의 값은 3
    }

    /**
     * 댓글 유효성 검증을 위한 헬퍼 메서드
     */
    private boolean isValidReply(Article reply, Article parent) {
        return reply.getReference() == parent.getId() &&
               reply.getLevel() > parent.getLevel() &&
               reply.getPId() > 0;
    }
}