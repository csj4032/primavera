package com.genius.primavera.domain.model.article;

import com.genius.primavera.domain.model.user.User;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Comment {
    private long id;
    private Article article;
    private int level;
    private int step;
    private User author;
    private String comment;
    private ArticleStatus status = ArticleStatus.PUBLIC;
    private Instant regDt;
    private Instant modDt;
}