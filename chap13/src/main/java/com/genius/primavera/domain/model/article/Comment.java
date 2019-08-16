package com.genius.primavera.domain.model.article;

import com.genius.primavera.domain.converter.ArticleStatusAttributeConverter;
import com.genius.primavera.domain.model.BaseEntity;
import com.genius.primavera.domain.model.user.User;

import java.time.Instant;

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
import javax.persistence.Table;

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