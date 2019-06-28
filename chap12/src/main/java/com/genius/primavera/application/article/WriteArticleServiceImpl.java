package com.genius.primavera.application.article;

import com.genius.primavera.application.storage.StorageService;
import com.genius.primavera.domain.mapper.article.ArticleAttachmentMapper;
import com.genius.primavera.domain.ArticleNotFoundException;
import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.Paged;
import com.genius.primavera.domain.mapper.article.ArticleCommentMapper;
import com.genius.primavera.domain.mapper.article.ArticleContentMapper;
import com.genius.primavera.domain.mapper.article.ArticleMapper;
import com.genius.primavera.domain.model.article.*;
import com.genius.primavera.domain.model.user.User;

import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;

import static com.genius.primavera.application.PrimaveraUtil.getUser;

@Service
@RequiredArgsConstructor
public class WriteArticleServiceImpl implements WriteArticleService {

	private final ModelMapper modelMapper;
	private final ArticleMapper articleMapper;
	private final ArticleContentMapper articleContentMapper;
	private final ArticleCommentMapper articleCommentMapper;
	private final ArticleAttachmentMapper articleAttachmentMapper;
	private final StorageService storageService;

	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMINISTRATOR')")
	public Article save(ArticleDto.WriteArticle writeArticle) {
		Article article = getArticle(getParentArticle(writeArticle), writeArticle, getUser());
		articleMapper.updateStep(article.getParentReference(), article.getParentStep());
		article.setContent(getContent(writeArticle, article));
		if (Objects.nonNull(writeArticle.getFile())) article.setSaveAttachment(getAttachment(writeArticle, article));
		saveArticle(article);
		return article;
	}

	@Override
	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_MANAGER', 'ROLE_ADMINISTRATOR')")
	public Article update(ArticleDto.WriteArticle writeArticle) {
		Article article = articleMapper.findByIdWithContent(writeArticle.getId());
		if (Objects.isNull(article)) throw new ArticleNotFoundException();
		article.setSubject(writeArticle.getSubject());
		article.setModDt(Instant.now());
		article.setContents(writeArticle.getContents());
		updateArticleAndContent(writeArticle, article);
		return article;
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMINISTRATOR')")
	public int delete(long id) {
		Article article = articleMapper.findById(id);
		if (Objects.isNull(article)) throw new ArticleNotFoundException();
		article.setStatus(ArticleStatus.DELETE);
		article.setModDt(Instant.now());
		return articleMapper.update(article);
	}

	@Override
	public ArticleDto.FormArticle findByForForm(WriteType type, long id) {
		if (type.equals(WriteType.REPLY)) return getReplayForm(id);
		if (type.equals(WriteType.UPDATE)) return getModifyForm(id);
		return getEmptyForm();
	}

	@Override
	public int comment(ArticleDto.WriteComment writeComment) {
		Article article = articleMapper.findById(writeComment.getArticle());
		return articleCommentMapper.save(getComment(writeComment, article));
	}

	@Override
	public Article findById(long id) {
		return articleMapper.findById(id);
	}

	@Override
	public ArticleDto.DetailArticle hitAndFindArticle(long id) {
		articleHit(id);
		return findByIdWithContentAndComment(id);
	}

	@Override
	public ArticleDto.DetailArticle findByIdWithContentAndComment(long id) {
		return modelMapper.map(articleMapper.findByIdWithContentAndComment(id), ArticleDto.DetailArticle.class);
	}

	@Override
	public Paged<ArticleDto.ListArticle> findForPageable(PageRequest pageRequest) {
		return new Paged<>(pageRequest, modelMapper.map(articleMapper.findForPageable(pageRequest), new TypeToken<List<ArticleDto.ListArticle>>() {
		}.getType()), articleMapper.findAllCount());
	}

	private void saveArticle(Article article) {
		articleMapper.save(article);
		articleContentMapper.save(article.getContent());
		if (Objects.nonNull(article.getSaveAttachment())) articleAttachmentMapper.save(article.getSaveAttachment());
	}

	private Attachment getAttachment(ArticleDto.WriteArticle writeArticle, Article article) {
		return Attachment.builder().article(article).file(getFile(writeArticle)).build();
	}

	private File getFile(ArticleDto.WriteArticle writeArticle) {
		storageService.store(writeArticle.getFile());
		return storageService.load(writeArticle.getFile().getOriginalFilename()).toFile();
	}

	@NotNull
	private Comment getComment(ArticleDto.WriteComment writeComment, Article article) {
		Comment comment = new Comment();
		comment.setArticle(article);
		comment.setAuthor(getUser());
		comment.setComment(writeComment.getComment());
		comment.setStatus(ArticleStatus.PUBLIC);
		comment.setRegDt(Instant.now());
		return comment;
	}

	private void articleHit(long id) {
		articleMapper.articleHit(id);
	}

	private Article getParentArticle(@NotNull ArticleDto.WriteArticle writeArticle) {
		if (writeArticle.getWriteType().equals(WriteType.REPLY)) {
			Article article = articleMapper.findById(writeArticle.getPId());
			if (Objects.isNull(article)) throw new ArticleNotFoundException();
			return article;
		}
		return new Article();
	}

	private Article getArticle(@NotNull Article parent, @NotNull ArticleDto.WriteArticle writeArticle, User author) {
		var article = new Article();
		article.setPId(parent.getId());
		article.setParent(parent);
		article.setReference(parent.getReference());
		article.setStep(parent.getStep() + 1);
		article.setLevel(parent.getLevel() + 1);
		article.setAuthor(author);
		article.setSubject(writeArticle.getSubject());
		article.setStatus(writeArticle.getStatus());
		article.setRegDt(Instant.now());
		return article;
	}

	private Content getContent(@NotNull ArticleDto.WriteArticle writeArticle, @NotNull Article article) {
		Content content = new Content();
		content.setArticle(article);
		content.setContents(writeArticle.getContents());
		return content;
	}

	private ArticleDto.FormArticle getEmptyForm() {
		return new ArticleDto.FormArticle();
	}

	private ArticleDto.FormArticle getModifyForm(long id) {
		ArticleDto.FormArticle formArticle = new ArticleDto.FormArticle();
		Article article = articleMapper.findByIdWithContent(id);
		formArticle.setId(id);
		formArticle.setSubject(article.getSubject());
		formArticle.setContents(article.getContents());
		formArticle.setWriteType(WriteType.UPDATE);
		return formArticle;
	}

	private ArticleDto.FormArticle getReplayForm(long id) {
		ArticleDto.FormArticle formArticle = new ArticleDto.FormArticle();
		Article article = articleMapper.findById(id);
		formArticle.setPId(id);
		formArticle.setSubject("[RE] " + article.getSubject());
		formArticle.setWriteType(WriteType.REPLY);
		return formArticle;
	}

	protected void updateArticleAndContent(ArticleDto.WriteArticle writeArticle, Article article) {
		articleMapper.update(article);
		articleContentMapper.update(article.getContentsId(), writeArticle.getContents());
	}
}