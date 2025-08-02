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
    
    // 하위 호환성을 위한 String 기반 메서드들
    public static void putContainer(String containerType, GenericContainer<?> container, ContainerLifecycleMode mode, String testClassName) {
        ContainerType type = ContainerType.valueOf(containerType.toUpperCase());
        putContainer(type, container, mode, testClassName);
    }

    public static GenericContainer<?> getContainer(String containerType, ContainerLifecycleMode mode, String testClassName) {
        ContainerType type = ContainerType.valueOf(containerType.toUpperCase());
        return getContainer(type, mode, testClassName);
    }

    public static boolean containsContainer(String containerType, ContainerLifecycleMode mode, String testClassName) {
        ContainerType type = ContainerType.valueOf(containerType.toUpperCase());
        return containsContainer(type, mode, testClassName);
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
    
    /**
     * 특정 테스트 클래스의 컨테이너들만 정리
     */
    public static void stopContainersByTestClass(String testClassName) {
        containers.entrySet().removeIf(entry -> {
            ContainerKey key = entry.getKey();
            if (testClassName.equals(key.getTestClassName())) {
                try {
                    GenericContainer<?> container = entry.getValue();
                    if (container.isRunning()) {
                        container.stop();
                        System.out.println("🛑 Stopped container for test class: " + key.getDisplayName());
                    }
                    return true;
                } catch (Exception e) {
                    System.err.println("Error stopping container " + key.getDisplayName() + ": " + e.getMessage());
                    return true;
                }
            }
            return false;
        });
    }
    
    /**
     * 현재 관리 중인 모든 컨테이너의 상태를 출력
     */
    public static void printContainerStatus() {
        System.out.println("📊 Container Status Report:");
        if (containers.isEmpty()) {
            System.out.println("  No containers are currently managed");
            return;
        }
        
        containers.forEach((key, container) -> {
            String status = container.isRunning() ? "RUNNING" : "STOPPED";
            System.out.println("  " + key.getDisplayName() + " -> " + status);
        });
    }
}