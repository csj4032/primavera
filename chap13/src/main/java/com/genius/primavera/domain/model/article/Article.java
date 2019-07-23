package com.genius.primavera.domain.model.article;

import com.genius.primavera.domain.model.user.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
@Entity
@Table(name = "ARTICLE")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "PID")
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
