package com.genius.primavera.domain.model.post;

import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum PostStatus {
    PUBLIC(1, "발행"),
    DELETE(2, "삭제"),
    BLOCK(3, "재제");

    private int value;
    private String name;

    PostStatus(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static PostStatus of(int source) {
        return Stream.of(PostStatus.values()).filter(postStatus -> postStatus.getValue() == source).findFirst().orElseThrow();
    }
}