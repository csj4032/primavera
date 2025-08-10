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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    public static void setUp() {
        user = User.builder().id(1L).nickname("SuperAdmin").build();
    }

    @Test
    @Order(1)
    @DisplayName("translated_text_3 1translated_text_1 translated_text_2")
    public void saveArticle1() {
        article1 = Article.builder().author(user).subject("translated_text_3 1translated_text_1").status(ArticleStatus.PUBLIC).createdAt(Instant.now()).build();
        int count = articleMapper.save(article1);
        assertEquals(1, count);
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_3 1translated_text_1_1translated_text_1 translated_text_2")
    public void saveArticle1_1() {
        article1_1 = Article.builder().parent(article1).pId(article1.getId()).reference(article1.getId()).step(1).level(1).author(user).subject("translated_text_3 1translated_text_1_1translated_text_1").status(ArticleStatus.PUBLIC).createdAt(Instant.now()).build();
        int count = articleMapper.save(article1_1);
        assertEquals(1, count);
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_3 1translated_text_1_1translated_text_1_1translated_text_1, 1translated_text_1_1translated_text_1_2translated_text_1 translated_text_2")
    public void saveArticle1_1_1() {
        article1_1_1 = Article.builder().parent(article1_1).pId(article1_1.getId()).reference(article1.getId()).step(2).level(2).author(user).subject("translated_text_3 1translated_text_1_1translated_text_1_1translated_text_1").status(ArticleStatus.PUBLIC).createdAt(Instant.now()).build();
        article1_1_2 = Article.builder().parent(article1_1).pId(article1_1.getId()).reference(article1.getId()).step(3).level(2).author(user).subject("translated_text_3 1translated_text_1_1translated_text_1_2translated_text_1").status(ArticleStatus.PUBLIC).createdAt(Instant.now()).build();
        int count = articleMapper.save(article1_1_1);
        assertEquals(1, count);
        count = articleMapper.save(article1_1_2);
        assertEquals(1, count);
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_3 2translated_text_1 translated_text_2")
    public void saveArticle2() {
        article2 = Article.builder().author(user).subject("translated_text_3 2translated_text_1").status(ArticleStatus.PUBLIC).createdAt(Instant.now()).build();
        int count = articleMapper.save(article2);
        assertEquals(1, count);
    }

    @Test
    @Order(5)
    @DisplayName("translated_text_3 2translated_text_1 translated_text_2")
    public void saveArticle2_1() {
        article2_1 = Article.builder().parent(article2).pId(article2.getId()).reference(article2.getId()).step(1).level(1).author(user).subject("translated_text_3 2translated_text_1_1translated_text_1").status(ArticleStatus.PUBLIC).createdAt(Instant.now()).build();
        int count = articleMapper.save(article2_1);
        assertEquals(1, count);
    }

    @Test
    @Order(6)
    @DisplayName("translated_text_3 inquiry")
    public void findAllArticle() {
        List<Article> articles = articleMapper.findAll();
        articles.forEach(article -> {
            if (article.getAuthor() != null) {
                System.out.println(" Author - ID: " + article.getAuthor().getId() + ", Nickname: " + article.getAuthor().getNickname() + ", Email: " + article.getAuthor().getEmail());
            } else {
                System.out.println(" Author is null for article ID: " + article.getId());
            }
        });
        assertTrue(articles.size() >= 6, "translated_text_2 6translated_text_2 translated_text_4 translated_text_3 translated_text_3. translated_text_2: " + articles.size());
        Article firstArticle = articles.get(0);
        Assertions.assertNotNull(firstArticle.getAuthor(), "Articletranslated_text_1 authortranslated_text_1 nulltranslated_text_2 translated_text_4");
        Assertions.assertNotNull(firstArticle.getAuthor().getNickname(), "Authortranslated_text_1 nicknametranslated_text_1 nulltranslated_text_2 translated_text_4");
    }
}