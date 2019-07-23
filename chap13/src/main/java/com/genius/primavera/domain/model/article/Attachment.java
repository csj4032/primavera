package com.genius.primavera.domain.model.article;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

import java.io.File;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ARTICLE_ATTACHMENT")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "ARTICLE_ID", nullable = false)
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