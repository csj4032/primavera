package com.genius.primavera.domain.model.post;

import com.genius.primavera.domain.converter.ConvertedEnum;
import com.genius.primavera.domain.converter.ReverseEnumResolver;
import com.genius.primavera.domain.model.user.RoleType;

import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum PostStatus implements ConvertedEnum<Integer>  {
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


    @Override
    public Integer toDbValue() {
        return value;
    }

    public static PostStatus fromDbValue(Integer dbValue) {
        return dbValueResolver.get(dbValue);
    }

    public Integer getDbValue() {
        return value;
    }

    public static final ReverseEnumResolver<PostStatus, Integer> dbValueResolver = new ReverseEnumResolver<>(PostStatus.class, PostStatus::getDbValue);
}