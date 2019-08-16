package com.genius.primavera.application.article;

import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.Paged;
import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleDto;
import com.genius.primavera.domain.model.article.Attachment;
import com.genius.primavera.domain.model.article.Comment;
import com.genius.primavera.domain.model.article.Content;
import com.genius.primavera.domain.model.article.WriteType;
import com.genius.primavera.domain.model.user.User;
import com.genius.primavera.domain.repository.ArticleRepository;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

import static com.genius.primavera.application.PrimaveraUtil.getUser;

@Service
@RequiredArgsConstructor
public class WriteArticleServiceImpl implements WriteArticleService {

	private final ArticleRepository articleRepository;

	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMINISTRATOR')")
	public Article save(ArticleDto.WriteArticle writeArticle) {
		Article article = getArticle(getParentArticle(writeArticle), writeArticle, getUser());
		articleRepository.save(article);
		article.setContent(getContent(writeArticle, article));
		//if (Objects.nonNull(writeArticle.getFile())) article.setSaveAttachment(getAttachment(writeArticle, article));
		saveArticle(article);
		return article;
	}

	@Override
	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_MANAGER', 'ROLE_ADMINISTRATOR')")
	public Article update(ArticleDto.WriteArticle writeArticle) {
		return null;
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMINISTRATOR')")
	public int delete(long id) {
		return 0;
	}

	@Override
	public ArticleDto.FormArticle findByForForm(WriteType type, long id) {
		if (type.equals(WriteType.REPLY)) return getReplayForm(id);
		if (type.equals(WriteType.UPDATE)) return getModifyForm(id);
		return getEmptyForm();
	}

	@Override
	public int comment(ArticleDto.WriteComment writeComment) {
		return 0;
	}

	@Override
	public Article findById(long id) {
		return articleRepository.findById(id).orElse(new Article());
	}

	@Override
	public ArticleDto.DetailArticle hitAndFindArticle(long id) {
		articleHit(id);
		return findByIdWithContentAndComment(id);
	}

	@Override
	public ArticleDto.DetailArticle findByIdWithContentAndComment(long id) {
		return null;
	}

	@Override
	public Paged<ArticleDto.ListArticle> findForPageable(PageRequest pageRequest) {
		return null;
	}

	private void saveArticle(Article article) {

	}

	private Attachment getAttachment(ArticleDto.WriteArticle writeArticle, Article article) {
		return Attachment.builder().article(article).file(storeAndLoad(writeArticle)).build();
	}

	private File storeAndLoad(ArticleDto.WriteArticle writeArticle) {
		return null;
	}

	@NotNull
	private Comment getComment(ArticleDto.WriteComment writeComment, Article article) {
		Comment comment = new Comment();
		return comment;
	}

	private void articleHit(long id) {

	}

	private Article getParentArticle(@NotNull ArticleDto.WriteArticle writeArticle) {
		return new Article();
	}

	private Article getArticle(@NotNull Article parent, @NotNull ArticleDto.WriteArticle writeArticle, User author) {
		var article = new Article();
		article.setPId(parent.getId());
		article.setReference(parent.getReference());
		article.setStep(parent.getStep() + 1);
		article.setLevel(parent.getLevel() + 1);
		article.setAuthor(author);
		article.setSubject(writeArticle.getSubject());
		article.setStatus(writeArticle.getStatus());
		article.setRegDt(LocalDateTime.now());
		return article;
	}

	private Content getContent(@NotNull ArticleDto.WriteArticle writeArticle, @NotNull Article article) {
		Content content = new Content();
		content.setContents(writeArticle.getContents());
		return content;
	}

	private ArticleDto.FormArticle getEmptyForm() {
		return new ArticleDto.FormArticle();
	}

	private ArticleDto.FormArticle getModifyForm(long id) {
		ArticleDto.FormArticle formArticle = new ArticleDto.FormArticle();
		return formArticle;
	}

	private ArticleDto.FormArticle getReplayForm(long id) {
		ArticleDto.FormArticle formArticle = new ArticleDto.FormArticle();
		return formArticle;
	}

	protected void updateArticleAndContent(ArticleDto.WriteArticle writeArticle, Article article) {
		articleRepository.save(article);
	}
}