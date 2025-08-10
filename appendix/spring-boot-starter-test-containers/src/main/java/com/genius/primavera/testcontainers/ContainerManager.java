package com.genius.primavera.testcontainers;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 하위 호환성을 위한 ContainerManager
 * 실제 구현은 ContainerLifecycleManager에 위임
 * 
 * @deprecated Use ContainerLifecycleManager directly
 */
@Slf4j
@Deprecated(since = "4.0", forRemoval = true)
public class ContainerManager {

    private final ContainerLifecycleManager delegate;

    public ContainerManager(EnableTestContainers annotation, Class<?> testClass) {
        this.delegate = new ContainerLifecycleManager(annotation, testClass);
        log.warn("ContainerManager is deprecated. Use ContainerLifecycleManager directly.");
    }

    public void startContainers() {
        delegate.startContainers();
    }

    public void stopContainers() {
        delegate.stopContainers();
    }

    public boolean isStarted() {
        return delegate.isStarted();
    }

    public Collection<ContainerInfo> getAllContainers() {
        return delegate.getAllContainers();
    }

    public ContainerInfo getContainer(String name) {
        return delegate.getContainer(name);
    }
}