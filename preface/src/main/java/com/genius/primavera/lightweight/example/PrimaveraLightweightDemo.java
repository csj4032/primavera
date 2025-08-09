package com.genius.primavera.lightweight.example;

import com.genius.primavera.lightweight.framework.PrimaveraApplication;
import com.genius.primavera.lightweight.framework.PrimaveraApplicationContext;
import lombok.extern.slf4j.Slf4j;

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