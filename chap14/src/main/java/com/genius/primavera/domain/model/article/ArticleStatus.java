package com.genius.primavera.domain.model.article;

import com.genius.primavera.domain.converter.ConvertedEnum;
import com.genius.primavera.domain.converter.ReverseEnumResolver;
import com.genius.primavera.domain.model.user.RoleType;

import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum ArticleStatus implements ConvertedEnum<Integer> {

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

    @Override
    public Integer toDbValue() {
        return value;
    }

    public static ArticleStatus fromDbValue(Integer dbValue) {
        return dbValueResolver.get(dbValue);
    }

    public Integer getDbValue() {
        return value;
    }

    public static final ReverseEnumResolver<ArticleStatus, Integer> dbValueResolver = new ReverseEnumResolver<>(ArticleStatus.class, ArticleStatus::getDbValue);
}
