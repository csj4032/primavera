package com.genius.primavera.testcontainer;

import org.testcontainers.containers.GenericContainer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContainerManager {

    private static final Map<String, GenericContainer<?>> globalContainers = new ConcurrentHashMap<>();
    private static final Map<String, GenericContainer<?>> classContainers = new ConcurrentHashMap<>();
    private static final ThreadLocal<Map<String, GenericContainer<?>>> testContainers = ThreadLocal.withInitial(ConcurrentHashMap::new);

    public static GenericContainer<?> getContainer(String containerType, ContainerLifecycleMode mode, String testClassName) {
        String key = containerType.toLowerCase();

        return switch (mode) {
            case REUSE -> globalContainers.get(key);
            case PER_CLASS -> classContainers.get(getClassKey(testClassName, key));
            case PER_TEST -> testContainers.get().get(key);
        };
    }

    public static void putContainer(String containerType, GenericContainer<?> container, ContainerLifecycleMode mode, String testClassName) {
        String key = containerType.toLowerCase();

        switch (mode) {
            case REUSE -> globalContainers.put(key, container);
            case PER_CLASS -> classContainers.put(getClassKey(testClassName, key), container);
            case PER_TEST -> testContainers.get().put(key, container);
        }
    }

    public static boolean containsContainer(String containerType, ContainerLifecycleMode mode, String testClassName) {
        String key = containerType.toLowerCase();

        return switch (mode) {
            case REUSE -> globalContainers.containsKey(key);
            case PER_CLASS -> classContainers.containsKey(getClassKey(testClassName, key));
            case PER_TEST -> testContainers.get().containsKey(key);
        };
    }

    private static String getClassKey(String testClassName, String containerType) {
        return testClassName + ":" + containerType;
    }

    public static void stopContainers(ContainerLifecycleMode mode) {
        Map<String, GenericContainer<?>> containers = switch (mode) {
            case REUSE -> globalContainers;
            case PER_CLASS -> classContainers;
            case PER_TEST -> testContainers.get();
        };

        containers.values().forEach(container -> {
            if (container.isRunning()) {
                container.stop();
            }
        });
        containers.clear();
    }

    public static void stopContainersForClass(String testClassName) {
        classContainers.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(testClassName + ":")) {
                GenericContainer<?> container = entry.getValue();
                if (container.isRunning()) {
                    container.stop();
                }
                return true;
            }
            return false;
        });
    }

    public static void cleanupTestContainers() {
        stopContainers(ContainerLifecycleMode.PER_TEST);
        testContainers.remove();
    }

    public static void cleanupClassContainers() {
        stopContainers(ContainerLifecycleMode.PER_CLASS);
    }

    public static void stopAllContainers() {
        stopContainers(ContainerLifecycleMode.REUSE);
        stopContainers(ContainerLifecycleMode.PER_CLASS);
        stopContainers(ContainerLifecycleMode.PER_TEST);
        testContainers.remove();
    }
}