package com.genius.primavera.domain.model.article;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Image {
    private String file;
    private String caption;
    private String colors;
    private String foreground;
    private String background;
}
