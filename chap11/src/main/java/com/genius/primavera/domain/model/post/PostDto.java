package com.genius.primavera.domain.model.post;

import java.time.Instant;
import java.util.Optional;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public sealed interface PostDto permits PostDto.RequestForSave, PostDto.ResponseForList {

    record RequestForSave(
        @NotEmpty String subject,
        @NotEmpty String contents,
        @NotNull Long writerId,
        PostStatus status,
        Instant createdAt
    ) implements PostDto {
        
        public static RequestForSave of(String subject, String contents, Long writerId) {
            return new RequestForSave(subject, contents, writerId, PostStatus.PUBLIC, Instant.now());
        }
        
        public static RequestForSave of(String subject, String contents, Long writerId, PostStatus status) {
            return new RequestForSave(subject, contents, writerId, status, Instant.now());
        }
        
        public boolean isPublic() {
            return status == PostStatus.PUBLIC;
        }
    }

    record ResponseForList(
        Long id,
        String subject,
        String writerNickName,
        Instant createdAt,
        Instant updatedAt
    ) implements PostDto {
        
        public static ResponseForList of(Long id, String subject, String writerNickName, Instant createdAt, Instant updatedAt) {
            return new ResponseForList(id, subject, writerNickName, createdAt, updatedAt);
        }
        
        public Optional<String> safeWriterNickName() {
            return Optional.ofNullable(writerNickName);
        }
        
        public boolean isModified() {
            return updatedAt != null && updatedAt.isAfter(createdAt);
        }
    }
}