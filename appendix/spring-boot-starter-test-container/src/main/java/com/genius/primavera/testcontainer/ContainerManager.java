package com.genius.primavera.testContainer;

import org.testcontainers.containers.GenericContainer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContainerManager {

    private static final Map<ContainerKey, GenericContainer<?>> containers = new ConcurrentHashMap<>();

    public static void putContainer(ContainerType containerType, GenericContainer<?> container, ContainerLifecycleMode mode, String testClassName) {
        ContainerKey key = ContainerKey.create(containerType, mode, testClassName);
        containers.put(key, container);
        System.out.println("📦 Container stored with key: " + key.getDisplayName());
    }

    public static GenericContainer<?> getContainer(ContainerType containerType, ContainerLifecycleMode mode, String testClassName) {
        ContainerKey key = ContainerKey.create(containerType, mode, testClassName);
        GenericContainer<?> container = containers.get(key);
        if (container != null) {
            System.out.println("🔍 Container found with key: " + key.getDisplayName());
        }
        return container;
    }

    public static boolean containsContainer(ContainerType containerType, ContainerLifecycleMode mode, String testClassName) {
        ContainerKey key = ContainerKey.create(containerType, mode, testClassName);
        boolean exists = containers.containsKey(key);
        System.out.println("❓ Container exists check for " + key.getDisplayName() + ": " + exists);
        return exists;
    }

    public static void stopContainers(ContainerLifecycleMode mode) {
        containers.entrySet().removeIf(entry -> {
            ContainerKey key = entry.getKey();
            if (key.getLifecycleMode() == mode) {
                try {
                    GenericContainer<?> container = entry.getValue();
                    if (container.isRunning()) {
                        container.stop();
                        System.out.println("🛑 Stopped container: " + key.getDisplayName());
                    }
                    return true; // 제거
                } catch (Exception e) {
                    System.err.println("Error stopping container " + key.getDisplayName() + ": " + e.getMessage());
                    return true; // 에러가 있어도 제거
                }
            }
            return false; // 유지
        });
    }

    public static void stopAllContainers() {
        containers.entrySet().removeIf(entry -> {
            ContainerKey key = entry.getKey();
            try {
                GenericContainer<?> container = entry.getValue();
                if (container.isRunning()) {
                    container.stop();
                    System.out.println("🛑 Stopped container: " + key.getDisplayName());
                }
                return true; // 모든 컨테이너 제거
            } catch (Exception e) {
                System.err.println("Error stopping container " + key.getDisplayName() + ": " + e.getMessage());
                return true; // 에러가 있어도 제거
            }
        });
    }
}