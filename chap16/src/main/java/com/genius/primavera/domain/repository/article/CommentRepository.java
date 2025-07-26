package com.genius.primavera.domain.repository.article;

import com.genius.primavera.domain.model.article.Comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByArticleId(long articleId);
}
