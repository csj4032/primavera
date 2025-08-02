package com.genius.primavera.testcontainer;

import org.testcontainers.containers.GenericContainer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContainerManager {

    private static final Map<String, GenericContainer<?>> globalContainers = new ConcurrentHashMap<>();
    private static final ThreadLocal<Map<String, GenericContainer<?>>> classContainers = ThreadLocal.withInitial(ConcurrentHashMap::new);
    private static final ThreadLocal<Map<String, GenericContainer<?>>> testContainers = ThreadLocal.withInitial(ConcurrentHashMap::new);

    public static GenericContainer<?> getContainer(String containerType, ContainerLifecycleMode mode) {
        String key = containerType.toLowerCase();

        return switch (mode) {
            case REUSE -> globalContainers.get(key);
            case PER_CLASS -> classContainers.get().get(key);
            case PER_TEST -> testContainers.get().get(key);
        };
    }

    public static void putContainer(String containerType, GenericContainer<?> container, ContainerLifecycleMode mode) {
        String key = containerType.toLowerCase();

        switch (mode) {
            case REUSE -> globalContainers.put(key, container);
            case PER_CLASS -> classContainers.get().put(key, container);
            case PER_TEST -> testContainers.get().put(key, container);
        }
    }

    public static boolean containsContainer(String containerType, ContainerLifecycleMode mode) {
        String key = containerType.toLowerCase();

        return switch (mode) {
            case REUSE -> globalContainers.containsKey(key);
            case PER_CLASS -> classContainers.get().containsKey(key);
            case PER_TEST -> testContainers.get().containsKey(key);
        };
    }

    public static void stopContainers(ContainerLifecycleMode mode) {
        Map<String, GenericContainer<?>> containers = switch (mode) {
            case REUSE -> globalContainers;
            case PER_CLASS -> classContainers.get();
            case PER_TEST -> testContainers.get();
        };

        containers.values().forEach(container -> {
            if (container.isRunning()) {
                container.stop();
            }
        });
        containers.clear();
    }

    public static void cleanupTestContainers() {
        stopContainers(ContainerLifecycleMode.PER_TEST);
        testContainers.remove();
    }

    public static void cleanupClassContainers() {
        stopContainers(ContainerLifecycleMode.PER_CLASS);
        classContainers.remove();
    }

    public static void stopAllContainers() {
        stopContainers(ContainerLifecycleMode.REUSE);
        stopContainers(ContainerLifecycleMode.PER_CLASS);
        stopContainers(ContainerLifecycleMode.PER_TEST);
        classContainers.remove();
        testContainers.remove();
    }
}