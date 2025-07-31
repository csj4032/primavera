package com.genius.primavera.lightweight.example;

import com.genius.primavera.lightweight.framework.PrimaveraApplication;
import com.genius.primavera.lightweight.framework.PrimaveraApplicationContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Primavera 경량 프레임워크 데모 애플리케이션
 * <p>
 * 이 클래스는 다음과 같은 기능들을 시연합니다:
 * 1. 컴포넌트 스캔 및 Bean 자동 등록
 * 2. 의존성 주입 (@PrimaveraAutowired)
 * 3. 설정 클래스를 통한 Bean 생성 (@PrimaveraConfiguration, @PrimaveraBean)
 * 4. ApplicationRunner를 통한 애플리케이션 시작 후 로직 실행
 * 5. 환경 설정 파일 로드 (application.properties)
 */
@Slf4j
public class PrimaveraLightweightDemo {

    public static void main(String[] args) {
        try {
            PrimaveraApplicationContext context = PrimaveraApplication.run(PrimaveraLightweightDemo.class, args);
            log.info("🌸 애플리케이션이 실행 중입니다. 종료하려면 Ctrl+C를 누르세요.");
            Thread.currentThread().join();
        } catch (Exception e) {
            log.error("애플리케이션 실행 중 오류 발생", e);
            System.exit(1);
        }
    }
}