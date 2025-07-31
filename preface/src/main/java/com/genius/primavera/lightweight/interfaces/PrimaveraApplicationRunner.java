package com.genius.primavera.lightweight.interfaces;

/**
 * Primavera 애플리케이션이 시작된 후 실행될 코드를 정의하는 인터페이스
 * Spring Boot의 ApplicationRunner와 유사한 역할을 합니다.
 * 
 * 이 인터페이스를 구현한 Bean들은 애플리케이션 시작이 완료된 후 자동으로 실행됩니다.
 */
@FunctionalInterface
public interface PrimaveraApplicationRunner {
    
    /**
     * 애플리케이션 시작 완료 후 수행할 작업을 정의합니다.
     * 
     * @throws Exception 실행 중 발생할 수 있는 예외
     */
    void run() throws Exception;
}