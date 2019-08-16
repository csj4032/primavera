package com.genius.primavera.domain.repository.article;

import com.genius.primavera.domain.model.article.Article;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Optional<Article> findById(long id);
}
