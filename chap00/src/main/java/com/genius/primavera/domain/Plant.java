package com.genius.primavera.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@EqualsAndHashCode(of = {"id", "name"})
public class Plant {
    private long id;
    private String name;
    private String location;
    private User manager;
    private LocalDateTime establishedDate;

    public Plant() {
    }

    public Plant(long id, String name, String location, User manager, LocalDateTime establishedDate) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.manager = manager;
        this.establishedDate = establishedDate;
    }
}
