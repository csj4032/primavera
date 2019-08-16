package com.genius.primavera.domain.repository.article;

import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleStatus;
import com.genius.primavera.domain.model.article.Content;
import com.genius.primavera.domain.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    @Test
    @Order(1)
    @DisplayName("게시글 관련 테이블 Truncate")
    public void cleanUp() {
        entityManager.createNativeQuery("TRUNCATE ARTICLE").executeUpdate();
        //entityManager.createNativeQuery("TRUNCATE ARTICLE_ATTACHMENT").executeUpdate();
        //entityManager.createNativeQuery("TRUNCATE ARTICLE_COMMENT").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE ARTICLE_CONTENT").executeUpdate();
    }

    @Test
    @Order(2)
    @DisplayName("게시글 작성 테스트")
    @Rollback(false)
    @Transactional
    public void writeArticleTest() {
        Article article = new Article();
        article.setPId(0);
        article.setReference(0);
        article.setStep(0);
        article.setAuthor(userRepository.findById(1l).orElse(null));
        article.setSubject("게시글 제목입니다. 1");
        article.setStatus(ArticleStatus.PUBLIC);
        article.setContent(Content.builder().contents("게시글 내용입니다. 1").build());
        articleRepository.save(article);
    }

    @Test
    @Order(3)
    @DisplayName("게시글 조회 테스트 [1번 글]")
    public void findByIdTest() {
        Article article = articleRepository.findById(1l).orElse(null);
        assertNotNull(article);
        assertNotNull(article.getComments());
        assertTrue(!article.getComments().isEmpty());
        assertEquals("게시글 제목입니다. 1", article.getSubject());
        assertEquals("Genius", article.getAuthorName());
        assertIterableEquals(article.getComments(), commentRepository.findByArticleId(1l));
    }

    @Test
    @Order(4)
    @DisplayName("게시글 수정 테스트 [1번 글]")
    @Rollback(false)
    @Transactional
    public void updateArticleTest() {
        Article article = articleRepository.findById(1).orElse(null);
        article.setSubject("게시글 제목입니다.(수정) 1");
        Content content = article.getContent();
        content.setContents("게시글 내용입니다.(수정) 1");
        articleRepository.save(article);
    }
}
