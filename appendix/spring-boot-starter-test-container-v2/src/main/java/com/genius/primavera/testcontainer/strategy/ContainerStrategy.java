package com.genius.primavera.testcontainer.strategy;

import com.genius.primavera.testcontainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;

/**
 * 컨테이너 생성 및 설정을 위한 전략 인터페이스
 */
public interface ContainerStrategy {
    
    /**
     * 컨테이너를 생성합니다.
     * 
     * @param config 컨테이너 설정
     * @return 생성된 컨테이너
     */
    GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config);
    
    /**
     * 애플리케이션 컨텍스트에 컨테이너 관련 속성을 설정합니다.
     * 
     * @param applicationContext 애플리케이션 컨텍스트
     * @param container 설정할 컨테이너
     */
    void configureApplicationContext(ConfigurableApplicationContext applicationContext, GenericContainer<?> container);
    
    /**
     * 이 전략이 지원하는 컨테이너 타입을 반환합니다.
     * 
     * @return 지원하는 컨테이너 타입
     */
    String getSupportedType();
}