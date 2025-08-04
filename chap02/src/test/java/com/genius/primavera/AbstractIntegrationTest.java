package com.genius.primavera;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * chap02 설정 관리 통합 테스트 추상 클래스
 * 
 * 특징:
 * - Spring Boot 설정 관리 테스트
 * - 프로퍼티 주입 및 검증
 * - JUnit 5 PER_CLASS + CONCURRENT 지원
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
public abstract class AbstractIntegrationTest {
    // chap02는 설정 관리 중심이므로 컨테이너 불필요
}