package com.genius.primavera.domain.model.article;

import com.genius.primavera.domain.model.user.User;

import java.time.Instant;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.*;

@Getter
@Setter
@Builder
@ToString(exclude = {"parent", "children", "comments"})
@NoArgsConstructor
@AllArgsConstructor
//@EqualsAndHashCode(of = "id")
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
	private Instant createAt;
	private Instant updatedAt;

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
		if (this.parent == null) return this;
		return parent.rootParent();
	}

	public long getParentId() {
		return this.pId;
	}

	public Article[] getSibling() {
		if (Objects.isNull(parent)) return null;
		return parent.getChildren();
	}

	public String toHierarchy() {
		return IntStream.range(0, this.level).mapToObj(e -> "--").collect(Collectors.joining()) + getId() + " " + getSubject() + " " + getAuthorName() + " " + getCreateAt();
	}
}
