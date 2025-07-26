package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.mapper.article.ArticleMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
@org.mybatis.spring.annotation.MapperScan("com.genius.primavera.domain.mapper")
class ArticleMapperTestApplication {
}

/**
 * ArticleMapper 통합 테스트
 * 
 * 환경:
 * - TestContainers MySQL 8.4.0 자동 관리
 * - Spring Security 비활성화
 * - MyBatis 매퍼 기반 데이터 접근
 */
@Slf4j
@SpringBootTest(classes = ArticleMapperTestApplication.class)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@Transactional
@Rollback(false)
class ArticleMapperTest {

	@Container
	static MariaDBContainer<?> mysql = new MariaDBContainer<>("mariadb:11.4.7")
		.withDatabaseName("primavera")
		.withUsername("primavera")
		.withPassword("primavera")
		.withInitScript("sql/schema.sql");

	@DynamicPropertySource
	static void configureTestProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysql::getJdbcUrl);
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
		registry.add("spring.flyway.enabled", () -> "false");
		registry.add("spring.sql.init.mode", () -> "never");
		
		log.info("🐳 TestContainers MySQL configured: {}", mysql.getJdbcUrl());
	}

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
		user = User.builder().id(1L).nickname("Genius").build();
		user2 = User.builder().id(2L).nickname("Son").build();
	}

	@Test
	@Order(1)
	@DisplayName("게시글 1번 저장")
	public void saveArticle1() {
		article1 = Article.builder().author(user).subject("게시글 1번").status(ArticleStatus.PUBLIC).createAt(Instant.now()).build();
		int count = articleMapper.save(article1);
		Assertions.assertEquals(1, count);
	}

	@Test
	@Order(2)
	@DisplayName("게시글 1번_1번 저장")
	public void saveArticle1_1() {
		article1_1 = Article.builder().parent(article1).pId(article1.getId()).reference(article1.getId()).step(1).level(1).author(user).subject("게시글 1번_1번").status(ArticleStatus.PUBLIC).createAt(Instant.now()).build();
		int count = articleMapper.save(article1_1);
		Assertions.assertEquals(1, count);
	}

	@Test
	@Order(3)
	@DisplayName("게시글 1번_1번_1번, 1번_1번_2번 저장")
	public void saveArticle1_1_1() {
		article1_1_1 = Article.builder().parent(article1_1).pId(article1_1.getId()).reference(article1.getId()).step(2).level(2).author(user).subject("게시글 1번_1번_1번").status(ArticleStatus.PUBLIC).createAt(Instant.now()).build();
		article1_1_2 = Article.builder().parent(article1_1).pId(article1_1.getId()).reference(article1.getId()).step(3).level(2).author(user).subject("게시글 1번_1번_2번").status(ArticleStatus.PUBLIC).createAt(Instant.now()).build();
		int count = articleMapper.save(article1_1_1);
		Assertions.assertEquals(1, count);
		count = articleMapper.save(article1_1_2);
		Assertions.assertEquals(1, count);
	}

	@Test
	@Order(4)
	@DisplayName("게시글 2번 저장")
	public void saveArticle2() {
		article2 = Article.builder().author(user).subject("게시글 2번").status(ArticleStatus.PUBLIC).createAt(Instant.now()).build();
		int count = articleMapper.save(article2);
		Assertions.assertEquals(1, count);
	}

	@Test
	@Order(5)
	@DisplayName("게시글 2번 저장")
	public void saveArticle2_1() {
		article2_1 = Article.builder().parent(article2).pId(article2.getId()).reference(article2.getId()).step(1).level(1).author(user).subject("게시글 2번_1번").status(ArticleStatus.PUBLIC).createAt(Instant.now()).build();
		int count = articleMapper.save(article2_1);
		Assertions.assertEquals(1, count);
	}

	@Test
	@Order(6)
	@DisplayName("게시글 조회")
	public void findAllArticle() {
		List<Article> articles = articleMapper.findAll();
		log.info("🔍 Article count: {}", articles.size());
		articles.forEach(article -> {
			log.info("📄 Article: {}", article.toString());
			if (article.getAuthor() != null) {
				log.info("👤 Author - ID: {}, Nickname: {}, Email: {}", 
					article.getAuthor().getId(), 
					article.getAuthor().getNickname(),
					article.getAuthor().getEmail());
			} else {
				log.warn("⚠️ Author is null for article ID: {}", article.getId());
			}
		});
		Assertions.assertEquals(6, articles.size());
		
		// 사용자 정보 검증
		Article firstArticle = articles.get(0);
		Assertions.assertNotNull(firstArticle.getAuthor(), "Article의 author가 null이면 안됩니다");
		Assertions.assertNotNull(firstArticle.getAuthor().getNickname(), "Author의 nickname이 null이면 안됩니다");
		log.info("✅ 사용자 정보 조회 성공: {} (ID: {})", firstArticle.getAuthor().getNickname(), firstArticle.getAuthor().getId());
	}
}