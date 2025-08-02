package com.genius.primavera.testcontainer;

import org.junit.jupiter.api.extension.*;

public class TestContainerLifecycleExtension implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        String testClassName = context.getRequiredTestClass().getName();
        ContainerManager.stopContainersForClass(testClassName);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        ContainerManager.cleanupTestContainers();
    }
}