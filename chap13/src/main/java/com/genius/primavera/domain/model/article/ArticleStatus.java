package com.genius.primavera.domain.model.article;

import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum ArticleStatus {

    PUBLIC(1, "발행"),
    DELETE(2, "삭제"),
    BLOCK(3, "재제");

    private int value;
    private String name;

    ArticleStatus(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static ArticleStatus of(int source) {
        return Stream.of(ArticleStatus.values()).filter(articleStatus -> articleStatus.getValue() == source).findFirst().orElseThrow();
    }
}
