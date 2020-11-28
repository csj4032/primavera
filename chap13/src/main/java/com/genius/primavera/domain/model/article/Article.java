package com.genius.primavera.domain.model.article;

import com.genius.primavera.domain.converter.ArticleStatusAttributeConverter;
import com.genius.primavera.domain.model.BaseEntity;
import com.genius.primavera.domain.model.user.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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
