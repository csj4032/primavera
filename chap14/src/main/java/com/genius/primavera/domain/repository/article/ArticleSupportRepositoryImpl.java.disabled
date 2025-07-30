package com.genius.primavera.domain.repository.article;

import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.QArticle;
import com.genius.primavera.domain.model.article.Search;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static com.genius.primavera.domain.model.article.QArticle.article;

@RequiredArgsConstructor
public class ArticleSupportRepositoryImpl implements ArticleSupportRepository {

	private final JPAQueryFactory queryFactory;

	public Page<Article> findBySearch(Search search, Pageable pageable) {
		QArticle article = QArticle.article;
		var result = queryFactory.selectFrom(article)
				.where(eqArticleId(search.getArticleId()), likeSubject(search.getTitle()))
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.orderBy(article.id.desc())
				.fetchResults();
		return new PageImpl<>(result.getResults(), pageable, result.getTotal());
	}

	private BooleanExpression eqArticleId(Long articleId) {
		if (ObjectUtils.isEmpty(articleId)) return null;
		return article.id.eq(articleId);
	}

	private BooleanExpression likeSubject(String subject) {
		if (ObjectUtils.isEmpty(subject)) return null;
		return article.subject.like(subject);
	}
}