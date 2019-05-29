package com.genius.primavera.application.article;

import com.genius.primavera.domain.ArticleNotFoundException;
import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.Paged;
import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleDto;
import com.genius.primavera.domain.model.article.WriteType;

public interface WriteArticleService {

    Article save(ArticleDto.WriteArticle writeArticle) throws ArticleNotFoundException;

    Article update(ArticleDto.WriteArticle writeArticle);

    int delete(long id);

    Article findById(long id);

    ArticleDto.DetailArticle findByIdWithContent(long id);

    Paged<ArticleDto.ListArticle> findForPageable(PageRequest pageRequest);

    ArticleDto.FormArticle findByForForm(WriteType type, long id);
}