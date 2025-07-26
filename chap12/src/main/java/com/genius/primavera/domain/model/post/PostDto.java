package com.genius.primavera.domain.model.post;

import java.time.Instant;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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
        @Builder.Default
        private Instant createAt = Instant.now();
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
        private Instant createAt;
        private Instant updatedAt;
    }
}