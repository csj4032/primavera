package com.genius.primavera.domain.model.article;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Content {
    private long id;
    private Article article;
    private String contents;
}