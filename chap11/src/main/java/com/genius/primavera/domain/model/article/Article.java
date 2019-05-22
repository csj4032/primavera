package com.genius.primavera.domain.model.article;

import com.genius.primavera.domain.model.user.User;

import java.time.Instant;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    @Builder.Default
    private int step = 1;
    private int maxStep = 1;
    @Builder.Default
    private int level = 1;
    private ArticleStatus status;
    private Article parent;
    private Article[] children;
    private String subject;
    private User author;
    private Content content;
    private Instant regDt;
    private Instant modDt;

    public long getAuthorId() {
        return author.getId();
    }

    public String getAuthorName() {
        return author.getNickname();
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

    public String toHierachy() {
        return IntStream.range(0, this.level).mapToObj(e -> "--").collect(Collectors.joining()) + getId() + " " + getSubject() + " " + getAuthorName() + " " + getRegDt();
    }
}
