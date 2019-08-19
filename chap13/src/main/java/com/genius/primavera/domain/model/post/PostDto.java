package com.genius.primavera.domain.model.post;

import java.time.Instant;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class PostDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RequestForSave {
        @NotEmpty
        private String subject;
        @NotEmpty
        private String contents;
        @NotNull
        private long writerId;
        @Builder.Default
        private PostStatus status = PostStatus.PUBLIC;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResponseForList {
        private long id;
        private String subject;
        private String writerNickName;
        private Instant regDt;
        private Instant modDt;
    }
}