package com.genius.primavera.application.article;

import com.genius.primavera.domain.ArticleNotFoundException;
import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleDto;
import com.genius.primavera.domain.model.article.WriteType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface WriteArticleService {

    Article save(ArticleDto.WriteArticle writeArticle) throws ArticleNotFoundException;

    Article update(ArticleDto.WriteArticle writeArticle);

    int delete(long id);

    Article findById(long id);

    ArticleDto.DetailArticle hitAndFindArticle(long id);

    ArticleDto.DetailArticle findByIdWithContentAndComment(long id);

    Page<ArticleDto.ListArticle> findForPageable(PageRequest pageRequest);

    ArticleDto.FormArticle findByForForm(WriteType type, long id);

    int comment(ArticleDto.WriteComment writeComment);
}