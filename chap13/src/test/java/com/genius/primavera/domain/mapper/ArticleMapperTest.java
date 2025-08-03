package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.mapper.article.ArticleMapper;
import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleStatus;
import com.genius.primavera.domain.model.user.User;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import static com.genius.primavera.testContainer.ContainerType.MARIADB;
import static com.genius.primavera.testContainer.ContainerType.MONGODB;

import com.genius.primavera.testContainer.ContainerLifecycleMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnablePrimaveraTestcontainers(containers = {MARIADB, MONGODB})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ArticleMapper 통합 테스트")
public class ArticleMapperTest {

    @Autowired
    private ArticleMapper articleMapper;

    private static User user;
    private static Article article1;
    private static Article article1_1;
    private static Article article1_1_1;
    private static Article article1_1_2;
    private static Article article2;
    private static Article article2_1;

    @BeforeAll
    static void setUp() {
        user = User.builder().id(1).nickname("Admin").build();
    }

    @Test
    @Order(1)
    @DisplayName("원글을 성공적으로 저장한다")
    void shouldSaveMainArticleSuccessfully() {
        article1 = Article.builder()
                .pId(0L)
                .step(1)
                .level(1)
                .author(user)
                .subject("게시글 1번")
                .status(ArticleStatus.PUBLIC)
                .createdAt(Instant.now())
                .build();
        int count = articleMapper.save(article1);
        assertEquals(1, count);
    }

    @Test
    @Order(2)
    @DisplayName("답글을 성공적으로 저장한다")
    void shouldSaveReplyArticleSuccessfully() {
        article1_1 = Article.builder()
                .parent(article1)
                .pId(article1.getId())
                .reference(article1.getReference() != 0 ? article1.getReference() : article1.getId())
                .step(2)
                .level(2)
                .author(user)
                .subject("게시글 1번_1번")
                .status(ArticleStatus.PUBLIC)
                .createdAt(Instant.now())
                .build();
        int count = articleMapper.save(article1_1);
        assertEquals(1, count);
    }

    @Test
    @Order(3)
    @DisplayName("계층형 답글을 성공적으로 저장한다")
    void shouldSaveNestedRepliesSuccessfully() {
        article1_1_1 = Article.builder()
                .parent(article1_1)
                .pId(article1_1.getId())
                .reference(article1.getReference() != 0 ? article1.getReference() : article1.getId())
                .step(3)
                .level(3)
                .author(user)
                .subject("게시글 1번_1번_1번")
                .status(ArticleStatus.PUBLIC)
                .createdAt(Instant.now())
                .build();
        article1_1_2 = Article.builder()
                .parent(article1_1)
                .pId(article1_1.getId())
                .reference(article1.getReference() != 0 ? article1.getReference() : article1.getId())
                .step(4)
                .level(3)
                .author(user)
                .subject("게시글 1번_1번_2번")
                .status(ArticleStatus.PUBLIC)
                .createdAt(Instant.now())
                .build();
        int count = articleMapper.save(article1_1_1);
        assertEquals(1, count);
        count = articleMapper.save(article1_1_2);
        assertEquals(1, count);
    }

    @Test
    @Order(4)
    @DisplayName("두 번째 원글을 성공적으로 저장한다")
    void shouldSaveSecondMainArticleSuccessfully() {
        article2 = Article.builder()
                .pId(0L)
                .step(1)
                .level(1)
                .author(user)
                .subject("게시글 2번")
                .status(ArticleStatus.PUBLIC)
                .createdAt(Instant.now())
                .build();
        int count = articleMapper.save(article2);
        assertEquals(1, count);
    }

    @Test
    @Order(5)
    @DisplayName("두 번째 원글의 답글을 성공적으로 저장한다")
    void shouldSaveSecondArticleReplySuccessfully() {
        article2_1 = Article.builder()
                .parent(article2)
                .pId(article2.getId())
                .reference(article2.getReference() != 0 ? article2.getReference() : article2.getId())
                .step(2)
                .level(2)
                .author(user)
                .subject("게시글 2번_1번")
                .status(ArticleStatus.PUBLIC)
                .createdAt(Instant.now())
                .build();
        int count = articleMapper.save(article2_1);
        assertEquals(1, count);
    }

//    @Test
//    @Order(6)
//    @DisplayName("모든 게시글을 성공적으로 조회한다")
//    void shouldFindAllArticlesSuccessfully() {
//        List<Article> articles = articleMapper.findAll();
//        assertEquals(30, articles.size());
//        articles.forEach(article -> log.info("Article: {}", article));
//    }
}