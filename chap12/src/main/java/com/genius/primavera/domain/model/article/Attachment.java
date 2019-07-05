package com.genius.primavera.domain.model.article;

import lombok.*;

import java.io.File;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
    private long id;
    private Article article;
    private String name;
    private long size;
    private String path;
    private File file;

    public boolean isExists() {
        return this.id != 0;
    }

    public long getArticleId() {
        return article.getId();
    }

    public static class AttachmentBuilder {
        private String name;
        private long size;
        private String path;
        private File file;
        public AttachmentBuilder file(File file) {
            this.file = file;
            this.name = file.getName();
            this.path = file.getAbsolutePath();
            this.size = file.length();
            return this;
        }
    }
}