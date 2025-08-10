package com.genius.primavera.domain.model.post;

import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum PostStatus {
    PUBLIC(1, "translated_text_2"),
    DELETE(2, "deletion"),
    BLOCK(3, "translated_text_2");

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