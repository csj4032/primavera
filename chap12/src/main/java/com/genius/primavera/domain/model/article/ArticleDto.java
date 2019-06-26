package com.genius.primavera.domain.model.article;

import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

public class ArticleDto {

    @Getter
    @Setter
    public static class WriteArticle {
        private long id;
        private long pId;
        private ArticleStatus status = ArticleStatus.PUBLIC;
        @NotEmpty
        private String subject;
        private long author;
        private String contents;
        private WriteType writeType = WriteType.FORM;
        private MultipartFile file;
    }

    @Getter
    @Setter
    public static class ListArticle {
        private long id;
        private int level;
        private String subject;
        private String authorName;
        private int hit;
        private int recommend;
        private int disapprove;
        private Instant regDt;
        private Instant modDt;

        public String getSubject() {
            return (level > 1 ? "[RE] " : "") + subject;
        }
    }

    @Getter
    @Setter
    public static class DetailArticle {
        private long id;
        private long reference;
        private int step;
        private int level;
        private String subject;
        private String authorName;
        private int hit;
        private int recommend;
        private int disapprove;
        private String contents;
        private CommentDto.Detail[] comments;
        private Instant regDt;
        private Instant modDt;

        public long getCommentSize() {
            return comments.length;
        }
    }

    @Getter
    @Setter
    public static class FormArticle {
        private long id;
        private long pId;
        private long reference;
        private int step;
        private int level;
        private String subject;
        private String authorName;
        private String contents;
        private WriteType writeType = WriteType.FORM;

        public String actionType() {
            return writeType.getAction();
        }

        public String writeType() {
            return writeType.getType();
        }
    }

    @Getter
    @Setter
    public static class WriteComment {
        private long article;
        private String comment;
    }
}
