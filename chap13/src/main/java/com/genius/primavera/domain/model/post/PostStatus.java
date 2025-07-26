package com.genius.primavera.domain.model.post;

import lombok.Getter;

@Getter
public enum PostStatus {
    PUBLIC(1, "발행"),
    DELETE(2, "삭제"),
    BLOCK(3, "파리채블락");

    private int value;
    private String name;

    PostStatus(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static PostStatus of(int source) {
        return switch (source) {
            case 1 -> PostStatus.PUBLIC;
            case 2 -> PostStatus.DELETE;
            case 3 -> PostStatus.BLOCK;
            default -> throw new IllegalStateException("Unexpected value: " + source);
        };
    }
}