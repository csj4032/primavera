package com.genius.primavera.domain.model.article;

import com.genius.primavera.domain.converter.ArticleStatusAttributeConverter;
import com.genius.primavera.domain.model.BaseEntity;
import com.genius.primavera.domain.model.user.User;

import java.time.Instant;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "ARTICLE_COMMENT")
public class Comment extends BaseEntity {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "ARTICLE_ID")
	private long articleId;

	@Column(name = "LEVEL")
	private int level;

	@Column(name = "STEP")
	private int step;

	@ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
	@JoinColumn(name = "AUTHOR", nullable = false, updatable = false)
	private User author;

	@Column(name = "COMMENT")
	private String comment;

	@Column(name = "STATUS")
	@Convert(converter = ArticleStatusAttributeConverter.class)
	private ArticleStatus status = ArticleStatus.PUBLIC;
}