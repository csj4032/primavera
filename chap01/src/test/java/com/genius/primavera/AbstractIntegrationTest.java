package com.genius.primavera;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * chap01 기본 Spring Boot 통합 테스트 추상 클래스
 * 
 * 특징:
 * - 데이터베이스 연동 없음
 * - 기본 Spring Boot 기능 테스트 중심
 * - JUnit 5 PER_CLASS + CONCURRENT 지원
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
public abstract class AbstractIntegrationTest {
    
    // chap01은 컨테이너가 필요 없으므로 기본 설정만 제공
    // 하위 테스트 클래스에서 이 클래스를 상속받아 사용
}