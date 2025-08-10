package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.mapper.article.ArticleMapper;
import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleStatus;
import com.genius.primavera.domain.model.user.User;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ArticleMapper test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class ArticleMapperTest {

    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", mariadb::getDriverClassName);
    }

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
    @DisplayName("connection successfully file")
    void shouldSaveMainArticleSuccessfully() {
        article1 = Article.builder()
                .pId(0L)
                .step(1)
                .level(1)
                .author(user)
                .subject("connection 1should")
                .status(ArticleStatus.PUBLIC)
                .createdAt(Instant.now())
                .build();
        int count = articleMapper.save(article1);
        assertEquals(1, count);
    }

    @Test
    @Order(2)
    @DisplayName("connection successfully file")
    void shouldSaveReplyArticleSuccessfully() {
        article1_1 = Article.builder()
                .parent(article1)
                .pId(article1.getId())
                .reference(article1.getReference() != 0 ? article1.getReference() : article1.getId())
                .step(2)
                .level(2)
                .author(user)
                .subject("connection 1should_1should")
                .status(ArticleStatus.PUBLIC)
                .createdAt(Instant.now())
                .build();
        int count = articleMapper.save(article1_1);
        assertEquals(1, count);
    }

    @Test
    @Order(3)
    @DisplayName("connection successfully file")
    void shouldSaveNestedRepliesSuccessfully() {
        article1_1_1 = Article.builder()
                .parent(article1_1)
                .pId(article1_1.getId())
                .reference(article1.getReference() != 0 ? article1.getReference() : article1.getId())
                .step(3)
                .level(3)
                .author(user)
                .subject("connection 1should_1should_1should")
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
                .subject("connection 1should_1should_2should")
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
    @DisplayName("needs to be added connection successfully file")
    void shouldSaveSecondMainArticleSuccessfully() {
        article2 = Article.builder()
                .pId(0L)
                .step(1)
                .level(1)
                .author(user)
                .subject("connection 2should")
                .status(ArticleStatus.PUBLIC)
                .createdAt(Instant.now())
                .build();
        int count = articleMapper.save(article2);
        assertEquals(1, count);
    }

    @Test
    @Order(5)
    @DisplayName("needs to be added connection successfully file")
    void shouldSaveSecondArticleReplySuccessfully() {
        article2_1 = Article.builder()
                .parent(article2)
                .pId(article2.getId())
                .reference(article2.getReference() != 0 ? article2.getReference() : article2.getId())
                .step(2)
                .level(2)
                .author(user)
                .subject("connection 2should_1should")
                .status(ArticleStatus.PUBLIC)
                .createdAt(Instant.now())
                .build();
        int count = articleMapper.save(article2_1);
        assertEquals(1, count);
    }

    @Test
    @Order(6)
    @DisplayName("all connection successfully should not")
    void shouldFindAllArticlesSuccessfully() {
        List<Article> articles = articleMapper.findAll();
        assertEquals(30, articles.size());
        articles.forEach(article -> log.info("Article: {}", article));
    }
}