package com.genius.primavera.lightweight.example.config;

import com.genius.primavera.lightweight.annotations.PrimaveraBean;
import com.genius.primavera.lightweight.annotations.PrimaveraConfiguration;
import lombok.extern.slf4j.Slf4j;

/**
 * 예제 설정 클래스
 * @PrimaveraBean 어노테이션을 사용한 Bean 생성 예제를 보여줍니다.
 */
@Slf4j
@PrimaveraConfiguration
public class ExampleConfiguration {
    
    /**
     * 애플리케이션 이름을 반환하는 Bean
     */
    @PrimaveraBean
    public String applicationName() {
        String name = "Primavera Lightweight Framework Demo";
        log.info("applicationName Bean 생성: {}", name);
        return name;
    }
    
    /**
     * 애플리케이션 버전을 반환하는 Bean
     */
    @PrimaveraBean
    public String applicationVersion() {
        String version = "1.0.0";
        log.info("applicationVersion Bean 생성: {}", version);
        return version;
    }
    
    /**
     * 최대 사용자 수를 반환하는 Bean
     */
    @PrimaveraBean("maxUsers")
    public Integer maxUserCount() {
        Integer maxUsers = 100;
        log.info("maxUsers Bean 생성: {}", maxUsers);
        return maxUsers;
    }
}