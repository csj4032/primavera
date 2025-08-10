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

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("ArticleService translated_text_2 test")
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
                .subject("test translated_text_3")
                .status(ArticleStatus.PUBLIC)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("translated_text_3 translated_text_2 - Mocktranslated_text_1 translated_text_3 translated_text_2 test")
    void shouldSaveArticleSuccessfully() {
        given(articleMapper.save(any(Article.class))).willReturn(1);

        var newArticle = Article.builder()
                .author(testUser)
                .subject("translated_text_3 translated_text_3")
                .status(ArticleStatus.PUBLIC)
                .createdAt(Instant.now())
                .build();
        var result = articleMapper.save(newArticle);

        assertEquals(1, result);
        verify(articleMapper).save(any(Article.class));
    }

    @Test
    @DisplayName("translated_text_3 translated_text_2 inquiry - Mock data translated_text_2")
    void shouldReturnArticleList() {

        List<Article> mockArticles = Arrays.asList(
                testArticle,
                Article.builder()
                        .id(2L)
                        .author(testUser)
                        .subject("translated_text_1 translated_text_2 translated_text_3")
                        .status(ArticleStatus.PUBLIC)
                        .createdAt(Instant.now())
                        .build()
        );
        
        given(articleMapper.findAll()).willReturn(mockArticles);

        List<Article> articles = articleMapper.findAll();

        assertEquals(2, articles.size());
        assertEquals("test translated_text_3", articles.get(0).getSubject());
        assertEquals("translated_text_1 translated_text_2 translated_text_3", articles.get(1).getSubject());
        verify(articleMapper).findAll();
    }

    @Test
    @DisplayName("translated_text_3 translated_text_2 translated_text_2 validation - translated_text_4 translated_text_2 test")
    void shouldValidateHierarchicalStructure() {

        Article parentArticle = Article.builder()
                .id(1L)
                .author(testUser)
                .subject("translated_text_2")
                .level(0)
                .step(0)
                .status(ArticleStatus.PUBLIC)
                .createdAt(Instant.now())
                .build();

        Article replyArticle = Article.builder()
                .id(2L)
                .parent(parentArticle)
                .pId(parentArticle.getId())
                .reference(parentArticle.getId())
                .author(testUser)
                .subject("translated_text_2")
                .level(1)
                .step(1)
                .status(ArticleStatus.PUBLIC)
                .createdAt(Instant.now())
                .build();

        assertTrue(isValidReply(replyArticle, parentArticle));
        assertEquals(parentArticle.getId(), replyArticle.getReference());
        assertEquals(parentArticle.getLevel() + 1, replyArticle.getLevel());
    }

    @Test
    @DisplayName("translated_text_3 translated_text_2 translated_text_2 validation")
    void shouldChangeArticleStatus() {

        Article article = Article.builder()
                .id(1L)
                .author(testUser)
                .subject("translated_text_2 translated_text_2 test")
                .status(ArticleStatus.PUBLIC)
                .build();

        Article updatedArticle = Article.builder()
                .id(article.getId())
                .author(article.getAuthor())
                .subject(article.getSubject())
                .status(ArticleStatus.DELETE)
                .build();

        assertEquals(ArticleStatus.DELETE, updatedArticle.getStatus());
        assertNotEquals(article.getStatus(), updatedArticle.getStatus());
    }

    @Test
    @DisplayName("translated_text_3 translated_text_2 translated_text_2 validation")
    void shouldBlockArticle() {

        Article article = Article.builder()
                .id(1L)
                .author(testUser)
                .subject("translated_text_2 translated_text_3")
                .status(ArticleStatus.PUBLIC)
                .build();

        article.setStatus(ArticleStatus.BLOCK);

        assertEquals(ArticleStatus.BLOCK, article.getStatus());
        assertEquals(3, article.getStatus().getValue());
    }

    private boolean isValidReply(Article reply, Article parent) {
        return reply.getReference() == parent.getId() &&
               reply.getLevel() > parent.getLevel() &&
               reply.getPId() > 0;
    }
}