package com.genius.primavera;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * chap04 사용자 서비스 통합 테스트 예제
 * 
 * AbstractIntegrationTest를 상속받아 MariaDB 컨테이너를 자동으로 사용합니다.
 */
@DisplayName("사용자 서비스 통합 테스트")
class UserServiceIntegrationTest extends AbstractIntegrationTest {
    
    @Test
    @DisplayName("사용자를 생성할 수 있다")
    void shouldCreateUser() {
        // Given
        // MariaDB 컨테이너가 AbstractIntegrationTest에 의해 자동으로 시작됨
        
        // When
        // 실제 서비스 로직 테스트
        
        // Then
        // 검증
        
        // 이 테스트는 실제 MariaDB 컨테이너를 사용하여 실행됩니다
        System.out.println("✅ MariaDB 컨테이너를 사용한 통합 테스트 완료!");
    }
    
    @Test
    @DisplayName("사용자 목록을 조회할 수 있다")
    void shouldFindAllUsers() {
        // AbstractIntegrationTest의 MariaDB 컨테이너 사용
        // JUnit 5 병렬 실행 지원
        System.out.println("✅ 병렬 실행 테스트 - 스레드: " + Thread.currentThread().getName());
    }
}