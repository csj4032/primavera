package com.genius.primavera.domain.model.post;

import java.time.Instant;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class PostDto {

    public record RequestForSave(
        Long id,
        @NotEmpty String subject,
        @NotEmpty String contents,
        @NotNull Long writerId,
        PostStatus status
    ) {
        public RequestForSave {
            if (status == null) {
                status = PostStatus.PUBLIC;
            }
        }
    }

    public record ResponseForList(
        long id,
        String subject,
        String writerNickName,
        Instant createdAt,
        Instant updatedAt
    ) {}
}