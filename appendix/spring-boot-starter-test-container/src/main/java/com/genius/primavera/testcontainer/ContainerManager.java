package com.genius.primavera.testContainer;

import org.testcontainers.containers.GenericContainer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContainerManager {

    private static final Map<ContainerKey, GenericContainer<?>> containers = new ConcurrentHashMap<>();

    public static void putContainer(ContainerType containerType, GenericContainer<?> container, ContainerLifecycleMode mode, String testClassName) {
        ContainerKey key = ContainerKey.create(containerType, mode, testClassName);
        containers.put(key, container);
    }

    public static GenericContainer<?> getContainer(ContainerType containerType, ContainerLifecycleMode mode, String testClassName) {
        ContainerKey key = ContainerKey.create(containerType, mode, testClassName);
        return containers.get(key);
    }

    public static boolean containsContainer(ContainerType containerType, ContainerLifecycleMode mode, String testClassName) {
        ContainerKey key = ContainerKey.create(containerType, mode, testClassName);
        return containers.containsKey(key);
    }

    public static void stopContainers(ContainerLifecycleMode mode) {
        containers.entrySet().removeIf(entry -> {
            ContainerKey key = entry.getKey();
            if (key.getLifecycleMode() == mode) {
                try {
                    GenericContainer<?> container = entry.getValue();
                    if (container.isRunning()) container.stop();
                    return true;
                } catch (Exception e) {
                    return true;
                }
            }
            return false;
        });
    }

    public static void stopAllContainers() {
        containers.entrySet().removeIf(entry -> {
            ContainerKey key = entry.getKey();
            try {
                GenericContainer<?> container = entry.getValue();
                if (container.isRunning()) container.stop();
                return true;
            } catch (Exception e) {
                return true;
            }
        });
    }
}