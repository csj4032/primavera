package com.genius.primavera.testContainer;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestContainerLifecycleExtension implements AfterAllCallback {

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        // 테스트 완료 후 PER_TEST 모드의 컨테이너들을 정리
        ContainerManager.stopContainers(ContainerLifecycleMode.PER_TEST);
    }
}