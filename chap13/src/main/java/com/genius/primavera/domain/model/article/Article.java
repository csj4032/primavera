package com.genius.primavera.domain.model.article;

import com.genius.primavera.domain.model.user.User;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Article {
	private long id;
	private long pId;
	private long reference;
	private int step;
	private int level;
	private ArticleStatus status;
	private Article parent;
	private Article[] children;
	private String subject;
	private User author;
	private int hit;
	private int recommend;
	private int disapprove;
	private Content content;
	private Comment[] comments;
	@Builder.Default
	private List<Attachment> attachments = new ArrayList<>();
	private Attachment saveAttachment;
	private Instant regDt;
	private Instant modDt;

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

	public boolean hasParents() {
		return !Objects.isNull(parent);
	}

	public boolean hasChildren() {
		return !Objects.isNull(children) && children.length > 0;
	}

	public Article rootParent() {
		if (this.getParentId() == 0) return this;
		return parent.getParent();
	}

	public long getParentId() {
		if (Objects.isNull(parent)) return 0;
		return this.parent.getId();
	}

	public Article[] getSibling() {
		if (Objects.isNull(parent)) return null;
		return parent.getChildren();
	}

	public void setContents(String contents) {
		content.setContents(contents);
	}

	public long getParentReference() {
		return parent.getReference();
	}

	public int getParentStep() {
		return parent.getStep();
	}
}
