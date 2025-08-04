package com.genius.primavera.domain.model.post;

import java.time.Instant;
import java.util.Optional;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 게시글 관련 DTO 클래스들을 Record로 현대화
 * Java 21+ Record와 Sealed Interface 패턴 활용
 */
public sealed interface PostDto permits PostDto.RequestForSave, PostDto.ResponseForList {

    /**
     * 게시글 저장 요청을 위한 불변 Record
     */
    record RequestForSave(
        @NotEmpty String subject,
        @NotEmpty String contents,
        @NotNull Long writerId,
        PostStatus status,
        Instant createdAt
    ) implements PostDto {
        
        /**
         * 기본값과 함께 생성하는 팩토리 메서드
         */
        public static RequestForSave of(String subject, String contents, Long writerId) {
            return new RequestForSave(subject, contents, writerId, PostStatus.PUBLIC, Instant.now());
        }
        
        /**
         * 전체 필드를 지정하는 팩토리 메서드
         */
        public static RequestForSave of(String subject, String contents, Long writerId, PostStatus status) {
            return new RequestForSave(subject, contents, writerId, status, Instant.now());
        }
        
        /**
         * 공개 게시글 여부 확인
         */
        public boolean isPublic() {
            return status == PostStatus.PUBLIC;
        }
    }

    /**
     * 게시글 목록 응답을 위한 불변 Record  
     */
    record ResponseForList(
        Long id,
        String subject,
        String writerNickName,
        Instant createdAt,
        Instant updatedAt
    ) implements PostDto {
        
        /**
         * 팩토리 메서드
         */
        public static ResponseForList of(Long id, String subject, String writerNickName, Instant createdAt, Instant updatedAt) {
            return new ResponseForList(id, subject, writerNickName, createdAt, updatedAt);
        }
        
        /**
         * 안전한 작성자명 접근
         */
        public Optional<String> safeWriterNickName() {
            return Optional.ofNullable(writerNickName);
        }
        
        /**
         * 수정 여부 확인
         */
        public boolean isModified() {
            return updatedAt != null && updatedAt.isAfter(createdAt);
        }
    }
}