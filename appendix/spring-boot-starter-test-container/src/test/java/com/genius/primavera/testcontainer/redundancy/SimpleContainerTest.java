package com.genius.primavera.testcontainer.redundancy;

import com.genius.primavera.testcontainer.config.MariaDBContainerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 간단한 컨테이너 기능 테스트 - Spring Context 없이
 */
@DisplayName("간단한 컨테이너 테스트")
@Slf4j
class SimpleContainerTest {

    @Test
    @DisplayName("MariaDBContainerConfiguration 클래스 로드 테스트")
    void testMariaDBContainerConfigurationClassLoad() {
        // 클래스 로드 확인
        Class<?> configClass = MariaDBContainerConfiguration.class;
        log.info("MariaDBContainerConfiguration 클래스 로드 성공: {}", configClass.getName());
        
        // 정적 메서드 호출 테스트
        int containerCount = MariaDBContainerConfiguration.getCachedContainerCount();
        log.info("현재 캐시된 컨테이너 개수: {}", containerCount);
    }
}