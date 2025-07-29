package com.genius.primavera.testContainer.strategy;

import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;

/**
 * TestContainer 관리를 위한 Strategy 인터페이스
 */
public interface ContainerStrategy {
    
    /**
     * 컨테이너를 시작하고 Spring Environment에 필요한 프로퍼티를 설정합니다.
     * 
     * @param context Spring Application Context
     * @return 시작된 컨테이너 인스턴스
     */
    GenericContainer<?> startContainer(ConfigurableApplicationContext context);
    
    /**
     * 이 Strategy가 지원하는 컨테이너 타입을 반환합니다.
     * 
     * @return 컨테이너 타입
     */
    String getContainerType();
    
    /**
     * 컨테이너가 이미 실행 중인지 확인합니다.
     * 
     * @return 실행 중이면 true, 아니면 false
     */
    boolean isRunning();
    
    /**
     * 실행 중인 컨테이너 인스턴스를 반환합니다.
     * 
     * @return 컨테이너 인스턴스 또는 null
     */
    GenericContainer<?> getContainer();
}