package com.genius.primavera;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * chap03 데이터 바인딩 통합 테스트 추상 클래스
 * 
 * 특징:
 * - JSON 데이터 바인딩 테스트
 * - 객체 변환 및 검증
 * - JUnit 5 PER_CLASS + CONCURRENT 지원
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
public abstract class AbstractIntegrationTest {
    // chap03은 데이터 바인딩 중심이므로 컨테이너 불필요
}