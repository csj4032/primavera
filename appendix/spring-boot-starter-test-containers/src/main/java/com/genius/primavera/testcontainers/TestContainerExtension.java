package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.bean.BeanCreatorRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.util.AnnotationUtils;

@Slf4j
public class TestContainerExtension implements TestInstancePreConstructCallback, BeforeAllCallback, AfterAllCallback {

    private static final String CONTAINER_MANAGER_KEY = "testcontainers.manager";
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(TestContainerExtension.class);

    @Override
    public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) throws Exception {
        BeanCreatorRegistry.initialize();

        Class<?> testClass = context.getRequiredTestClass();
        String testClassName = testClass.getName();

        Object classLock = ContainerRegistry.getLock(testClassName);
        synchronized (classLock) {
            EnableTestContainers annotation = AnnotationUtils
                    .findAnnotation(testClass, EnableTestContainers.class)
                    .orElse(null);

            if (annotation != null) {
                log.info("Setting up containers for test class: {}", testClassName);
                ContainerManager manager = getOrCreateContainerManager(context, annotation, testClass);
                ContainerRegistry.register(manager);
                manager.startContainers();
                log.info("All containers set up for test class: {}", testClassName);
            }
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        ContainerManager manager = getContainerManager(context);
        if (manager != null && !manager.isStarted()) {
            log.info("Starting containers in beforeAll for class: {}", context.getRequiredTestClass().getSimpleName());
            manager.startContainers();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        String testClassName = context.getRequiredTestClass().getName();
        Object classLock = ContainerRegistry.getLock(testClassName);

        synchronized (classLock) {
            ContainerManager manager = getContainerManager(context);
            if (manager != null) {
                log.info("Stopping containers for test class: {}", testClassName);
                manager.stopContainers();
                log.info("All containers stopped for test class: {}", testClassName);
            }

            ContainerRegistry.clear();
            ContainerRegistry.removeLock(testClassName);
        }
    }

    private ContainerManager getOrCreateContainerManager(ExtensionContext context,
                                                                  EnableTestContainers annotation,
                                                                  Class<?> testClass) {
        ExtensionContext.Store store = getStore(context);
        return store.getOrComputeIfAbsent(
                CONTAINER_MANAGER_KEY,
                key -> new ContainerManager(annotation, testClass),
                ContainerManager.class
        );
    }

    private ContainerManager getContainerManager(ExtensionContext context) {
        return getStore(context).get(CONTAINER_MANAGER_KEY, ContainerManager.class);
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }
}