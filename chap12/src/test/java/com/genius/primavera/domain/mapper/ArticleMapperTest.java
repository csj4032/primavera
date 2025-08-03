package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.mapper.article.ArticleMapper;
import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleStatus;
import com.genius.primavera.domain.model.user.User;
import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
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
@EnablePrimaveraTestcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ArticleMapperTest {

    @Autowired
    private ArticleMapper articleMapper;

    private static User user;
    private static User user2;
    private static Article article1;
    private static Article article1_1;
    private static Article article1_1_1;
    private static Article article1_1_2;
    private static Article article2;
    private static Article article2_1;

    @BeforeAll
    public static void setUp() {
        user = User.builder().id(100L).nickname("Genius").build();
        user2 = User.builder().id(200L).nickname("Son").build();
    }

    @Test
    @Order(1)
    @DisplayName("게시글 1번 저장")
    public void saveArticle1() {
        article1 = Article.builder().author(user).subject("게시글 1번").status(ArticleStatus.PUBLIC).createdAt(Instant.now()).build();
        int count = articleMapper.save(article1);
        assertEquals(1, count);
    }

    @Test
    @Order(2)
    @DisplayName("게시글 1번_1번 저장")
    public void saveArticle1_1() {
        article1_1 = Article.builder().parent(article1).pId(article1.getId()).reference(article1.getId()).step(1).level(1).author(user).subject("게시글 1번_1번").status(ArticleStatus.PUBLIC).createdAt(Instant.now()).build();
        int count = articleMapper.save(article1_1);
        assertEquals(1, count);
    }

    @Test
    @Order(3)
    @DisplayName("게시글 1번_1번_1번, 1번_1번_2번 저장")
    public void saveArticle1_1_1() {
        article1_1_1 = Article.builder().parent(article1_1).pId(article1_1.getId()).reference(article1.getId()).step(2).level(2).author(user).subject("게시글 1번_1번_1번").status(ArticleStatus.PUBLIC).createdAt(Instant.now()).build();
        article1_1_2 = Article.builder().parent(article1_1).pId(article1_1.getId()).reference(article1.getId()).step(3).level(2).author(user).subject("게시글 1번_1번_2번").status(ArticleStatus.PUBLIC).createdAt(Instant.now()).build();
        int count = articleMapper.save(article1_1_1);
        assertEquals(1, count);
        count = articleMapper.save(article1_1_2);
        assertEquals(1, count);
    }

    @Test
    @Order(4)
    @DisplayName("게시글 2번 저장")
    public void saveArticle2() {
        article2 = Article.builder().author(user).subject("게시글 2번").status(ArticleStatus.PUBLIC).createdAt(Instant.now()).build();
        int count = articleMapper.save(article2);
        assertEquals(1, count);
    }

    @Test
    @Order(5)
    @DisplayName("게시글 2번 저장")
    public void saveArticle2_1() {
        article2_1 = Article.builder().parent(article2).pId(article2.getId()).reference(article2.getId()).step(1).level(1).author(user).subject("게시글 2번_1번").status(ArticleStatus.PUBLIC).createdAt(Instant.now()).build();
        int count = articleMapper.save(article2_1);
        assertEquals(1, count);
    }

    @Test
    @Order(6)
    @DisplayName("게시글 조회")
    public void findAllArticle() {
        List<Article> articles = articleMapper.findAll();
        articles.forEach(article -> {
            if (article.getAuthor() != null) {
                System.out.println("👤 Author - ID: " + article.getAuthor().getId() + ", Nickname: " + article.getAuthor().getNickname() + ", Email: " + article.getAuthor().getEmail());
            } else {
                System.out.println("⚠️ Author is null for article ID: " + article.getId());
            }
        });
        assertTrue(articles.size() >= 6, "최소 6개의 아티클이 있어야 합니다. 실제: " + articles.size());
        Article firstArticle = articles.get(0);
        Assertions.assertNotNull(firstArticle.getAuthor(), "Article의 author가 null이면 안됩니다");
        Assertions.assertNotNull(firstArticle.getAuthor().getNickname(), "Author의 nickname이 null이면 안됩니다");
    }
}