package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleStatus;
import com.genius.primavera.domain.model.user.User;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ArticleMapperTest {

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
        user = User.builder().id(1).nickname("최성조").build();
        user2 = User.builder().id(2).nickname("홍길동").build();
    }

    @Test
    @Order(1)
    @DisplayName("게시글 1번 저장")
    public void saveArticle1() {
        article1 = Article.builder().author(user).subject("게시글 1번").status(ArticleStatus.PUBLIC).regDt(Instant.now()).build();
        int count = articleMapper.save(article1);
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(2)
    @DisplayName("게시글 1번_1번 저장")
    public void saveArticle1_1() {
        article1_1 = Article.builder().parent(article1).pId(article1.getId()).reference(article1.getId()).step(1).level(1).author(user).subject("게시글 1번_1번").status(ArticleStatus.PUBLIC).regDt(Instant.now()).build();
        int count = articleMapper.save(article1_1);
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(3)
    @DisplayName("게시글 1번_1번_1번, 1번_1번_2번 저장")
    public void saveArticle1_1_1() {
        article1_1_1 = Article.builder().parent(article1_1).pId(article1_1.getId()).reference(article1.getId()).step(2).level(2).author(user).subject("게시글 1번_1번_1번").status(ArticleStatus.PUBLIC).regDt(Instant.now()).build();
        article1_1_2 = Article.builder().parent(article1_1).pId(article1_1.getId()).reference(article1.getId()).step(3).level(2).author(user).subject("게시글 1번_1번_2번").status(ArticleStatus.PUBLIC).regDt(Instant.now()).build();
        int count = articleMapper.save(article1_1_1);
        Assertions.assertEquals(1, count);
        count = articleMapper.save(article1_1_2);
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(4)
    @DisplayName("게시글 2번 저장")
    public void saveArticle2() {
        article2 = Article.builder().author(user).subject("게시글 2번").status(ArticleStatus.PUBLIC).regDt(Instant.now()).build();
        int count = articleMapper.save(article2);
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(5)
    @DisplayName("게시글 2번 저장")
    public void saveArticle2_1() {
        article2_1 = Article.builder().parent(article2).pId(article2.getId()).reference(article2.getId()).step(1).level(1).author(user).subject("게시글 2번_1번").status(ArticleStatus.PUBLIC).regDt(Instant.now()).build();
        int count = articleMapper.save(article2_1);
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(6)
    @DisplayName("게시글 조회")
    public void findAllArticle() {
        List<Article> articles = articleMapper.findAll();
        Assertions.assertEquals(2, articles.size());
        articles.forEach(e -> System.out.println(e.toString()));
    }
}