package com.genius.primavera.hikari;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
@DisplayName("HikariCP 설정별 성능 비교 가이드")
public class HikariPerformanceComparisonSuite {

    @Test
    @DisplayName("HikariCP 성능 비교 테스트 가이드")
    void performanceComparisonGuide() {
        log.info("=== HikariCP 성능 비교 테스트 스위트 ===");
        log.info("");
        log.info("📊 테스트 설정별 비교 포인트:");
        log.info("");
        log.info("1. 🔵 Minimal Pool (최소 설정)");
        log.info("   - Pool Size: 1~3 connections");
        log.info("   - 특징: 메모리 절약, 동시성 제한");
        log.info("   - 적용: 리소스가 매우 제한된 환경");
        log.info("");
        log.info("2. 🟢 Balanced Pool (균형 설정)");
        log.info("   - Pool Size: 5~10 connections");
        log.info("   - 특징: 성능과 안정성의 균형");
        log.info("   - 적용: 일반적인 운영 환경");
        log.info("");
        log.info("3. 🟠 Performance Pool (성능 중심)");
        log.info("   - Pool Size: 10~20 connections");
        log.info("   - 특징: 최고 처리량, 높은 메모리 사용");
        log.info("   - 적용: 고성능 요구 환경");
        log.info("");
        log.info("4. 🔴 Resource-Constrained Pool (리소스 제약)");
        log.info("   - Pool Size: 2~5 connections");
        log.info("   - 특징: 최소 메모리, 엄격한 관리");
        log.info("   - 적용: 컨테이너나 임베디드 환경");
        log.info("");
        log.info("💡 각 테스트를 개별 실행하여 설정별 성능 특성을 비교해보세요!");
        log.info("📈 로그에서 처리량(queries/sec), 대기시간, 풀 통계를 확인하세요!");
        log.info("");
    }
}