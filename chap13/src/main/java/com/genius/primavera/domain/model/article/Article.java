package com.genius.primavera.domain.model.article;

import com.genius.primavera.domain.converter.ArticleStatusAttributeConverter;
import com.genius.primavera.domain.model.BaseEntity;
import com.genius.primavera.domain.model.user.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Builder
@ToString
@Entity
@Table(name = "ARTICLE")
@NoArgsConstructor
@AllArgsConstructor
public class Article extends BaseEntity {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "P_ID")
	private long pId;

	@Column(name = "REFERENCE")
	private long reference;

	@Column(name = "STEP")
	private int step;

	@Column(name = "LEVEL")
	private int level;

	@Column(name = "STATUS")
	@Convert(converter = ArticleStatusAttributeConverter.class)
	private ArticleStatus status;

	@Column(name = "SUBJECT")
	private String subject;

	@ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
	@JoinColumn(name = "AUTHOR", nullable = false, updatable = false)
	private User author;

	@Column(name = "HIT")
	private int hit;

	@Column(name = "RECOMMEND")
	private int recommend;

	@Column(name = "DISAPPROVE")
	private int disapprove;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "CONTENT_ID", referencedColumnName = "ID")
	private Content content;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "ARTICLE_ID")
	private List<Comment> comments;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "ARTICLE_ID")
	private List<Attachment> attachments;

	public long getAuthorId() {
		return author.getId();
	}

	public String getAuthorName() {
		return author.getNickname();
	}

	public String getContents() {
		return content.getContents();
	}

	public long getContentsId() {
		return content.getId();
	}

	public void setContents(String contents) {
		content.setContents(contents);
	}
}
