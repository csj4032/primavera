package com.genius.primavera.testcontainer;

import org.junit.jupiter.api.extension.*;

public class TestContainerLifecycleExtension implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {
    
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        // 클래스 레벨에서 필요한 초기화
    }
    
    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        // PER_CLASS 모드의 컨테이너 정리
        ContainerManager.cleanupClassContainers();
    }
    
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        // 각 테스트 전에 필요한 초기화
    }
    
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        // PER_TEST 모드의 컨테이너 정리
        ContainerManager.cleanupTestContainers();
    }
}