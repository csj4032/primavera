package com.genius.primavera.domain.model.article;

import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum WriteType {
    FORM("form", "save"),
    MODIFY("modify", "modify"),
    REPLY("reply", "save");

    private String type;
    private String action;

    WriteType(String type, String action) {
        this.type = type;
        this.action = action;
    }

    public static WriteType of(String source) {
        return Stream.of(WriteType.values()).filter(writeType -> writeType.getType().equalsIgnoreCase(source)).findFirst().orElseThrow();
    }
}