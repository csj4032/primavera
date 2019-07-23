package com.genius.primavera.domain.repository;

import com.genius.primavera.domain.model.article.Article;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Article findById(long id);
}
