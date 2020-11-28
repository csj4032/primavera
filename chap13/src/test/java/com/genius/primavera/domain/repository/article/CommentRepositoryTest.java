package com.genius.primavera.domain.repository.article;

import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleStatus;
import com.genius.primavera.domain.model.article.Comment;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentRepositoryTest {

	@Autowired
	private ArticleRepository articleRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	@Order(1)
	@Rollback(false)
	@Transactional
	@DisplayName("게시글 1번 댓글 작성 테스트")
	public void writeComment() {
		Article article = articleRepository.findById(1).orElseThrow();
		Comment comment = new Comment();
		comment.setArticleId(article.getId());
		comment.setLevel(0);
		comment.setStep(0);
		comment.setComment("댓글입니다.");
		comment.setAuthor(userRepository.findById(1l).get());
		comment.setStatus(ArticleStatus.PUBLIC);
		commentRepository.save(comment);
	}
}
