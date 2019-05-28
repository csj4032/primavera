package com.genius.primavera.application.article;

import com.genius.primavera.domain.ArticleNotFoundException;
import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.Paged;
import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleDto;

public interface WriteArticleService {

    Article write(ArticleDto.WriteArticle writeArticle) throws ArticleNotFoundException;

    Article findById(long id);

    ArticleDto.DetailArticle findByIdWithContent(long id);

    Paged<ArticleDto.ListArticle> findForPageable(PageRequest pageRequest);

    String getOriginSubject(long originId);
}