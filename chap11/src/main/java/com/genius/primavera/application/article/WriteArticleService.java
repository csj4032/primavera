package com.genius.primavera.application.article;

import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleDto;

public interface WriteArticleService {

    Article write(ArticleDto.WriteRequestArticle requestArticle);

    Article findById(long id);
}