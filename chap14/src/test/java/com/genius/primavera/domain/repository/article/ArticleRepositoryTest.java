package com.genius.primavera.domain.repository.article;

import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleStatus;
import com.genius.primavera.domain.model.article.Attachment;
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

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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
	@DisplayName("translated_text_3 translated_text_2 translated_text_3 Truncate")
	public void cleanUp() {
		entityManager.createNativeQuery("TRUNCATE ARTICLE").executeUpdate();
		entityManager.createNativeQuery("TRUNCATE ARTICLE_ATTACHMENT").executeUpdate();
		entityManager.createNativeQuery("TRUNCATE ARTICLE_CONTENT").executeUpdate();
	}

	@Test
	@Order(2)
	@DisplayName("translated_text_3 translated_text_2 test")
	@Rollback(false)
	@Transactional
	public void writeArticleTest() {
		Article article = new Article();
		article.setPId(0);
		article.setReference(0);
		article.setStep(0);
		article.setAuthor(userRepository.findById(1l).orElse(null));
		article.setSubject("translated_text_3 translated_text_5. 1");
		article.setStatus(ArticleStatus.PUBLIC);
		article.setContent(Content.builder().contents("translated_text_3 translated_text_5. 1").build());
		article.setAttachments(List.of(Attachment.builder().name("file1.txt").path("/path").size(100).build()));
		articleRepository.save(article);
	}

	@Test
	@Order(3)
	@DisplayName("translated_text_3 inquiry test [1translated_text_1 translated_text_1]")
	public void findByIdTest() {
		Article article = articleRepository.findById(1l).orElse(null);

		assertNotNull(article);
		assertEquals("translated_text_3 translated_text_5. 1", article.getSubject());
		assertEquals("Genius", article.getAuthorName());

		assertNotNull(article.getComments());
		assertTrue(!article.getComments().isEmpty());
		assertIterableEquals(article.getComments(), commentRepository.findByArticleId(1l));

		assertNotNull(article.getAttachments());
		assertTrue(!article.getAttachments().isEmpty());
	}

	@Test
	@Order(4)
	@DisplayName("translated_text_3 modification test [1translated_text_1 translated_text_1]")
	@Rollback(false)
	@Transactional
	public void updateArticleTest() {
		Article article = articleRepository.findById(1).orElse(null);
		article.setSubject("translated_text_3 translated_text_5.(modification) 1");
		Content content = article.getContent();
		content.setContents("translated_text_3 translated_text_5.(modification) 1");
		article.getAttachments().add(Attachment.builder().name("file2.txt").path("/path").size(100).build());
		articleRepository.save(article);
	}

	@Test
	@Order(5)
	@DisplayName("translated_text_3 modification translated_text_1 inquiry test [1translated_text_1 translated_text_1]")
	public void updateAfterFindByIdTest() {
		Article article = articleRepository.findById(1l).orElse(null);
		assertNotNull(article);
		assertEquals("translated_text_3 translated_text_5.(modification) 1", article.getSubject());
		assertEquals("translated_text_3 translated_text_5.(modification) 1", article.getContents());

		assertNotNull(article.getAttachments());
		assertTrue(article.getAttachments().size() == 2);
		article.getAttachments().forEach(e -> assertEquals("/path", e.getPath()));
	}
}