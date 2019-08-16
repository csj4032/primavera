package com.genius.primavera.domain.model.article;

import com.genius.primavera.domain.model.user.User;

import java.time.Instant;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ARTICLE_COMMENT")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "LEVEL")
    private int level;

    @Column(name = "STEP")
    private int step;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User author;
    private String comment;
    private ArticleStatus status = ArticleStatus.PUBLIC;
    private Instant regDt;
    private Instant modDt;
}