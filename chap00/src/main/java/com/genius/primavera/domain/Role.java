package com.genius.primavera.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode(of = {"id", "name"})
public class Role {
    private long id;
    private String name;
    private String description;

    public Role() {
    }

    public Role(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
