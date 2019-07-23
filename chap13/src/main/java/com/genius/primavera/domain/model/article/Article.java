package com.genius.primavera.domain.model.article;

import com.genius.primavera.domain.model.user.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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

    @Column(name = "REFERENCE")
    private long reference;

    @Column(name = "STEP")
    private int step;

    @Column(name = "LEVEL")
    private int level;

    private ArticleStatus status;

    private String subject;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User author;

    private int hit;
    private int recommend;
    private int disapprove;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "article")
    private Content content;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "ARTICLE_ID")
    private List<Comment> comments;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "article")
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();

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

    public void setContents(String contents) {
        content.setContents(contents);
    }
}
