package com.genius.primavera.domain.repository.article;

import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.Search;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleSupportRepository {

    Page<Article> findBySearch(Search search, Pageable pageable);
}