package com.genius.primavera.testcontainer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;

/**
 * TestContainers 공통 설정
 */
@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class TestContainerCommonConfiguration {
    
    public TestContainerCommonConfiguration() {
        log.info("TestContainers 공통 설정이 로드되었습니다.");
    }
}