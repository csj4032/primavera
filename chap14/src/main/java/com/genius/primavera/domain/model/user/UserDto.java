package com.genius.primavera.domain.model.user;

import java.util.Optional;

/**
 * 사용자 정보를 전송하기 위한 불변 Record DTO
 * Java 21+ Record 패턴을 사용하여 보일러플레이트 코드 제거
 */
public record UserDto(
    Long id,
    String email,
    String nickname,
    UserStatus status
) {
    
    /**
     * 빌더 패턴을 위한 정적 팩토리 메서드
     */
    public static UserDto of(Long id, String email, String nickname, UserStatus status) {
        return new UserDto(id, email, nickname, status);
    }
    
    /**
     * 활성 사용자 여부를 확인하는 편의 메서드
     */
    public boolean isActive() {
        return status == UserStatus.ON;
    }
    
    /**
     * Optional을 사용한 안전한 접근자
     */
    public Optional<String> safeEmail() {
        return Optional.ofNullable(email);
    }
}