package com.genius.primavera.testcontainer.v2;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class ContainerManager {
    
    private static final ConcurrentMap<ContainerKey, GenericContainer<?>> containers = new ConcurrentHashMap<>();
    
    public static void putContainer(ContainerType type, GenericContainer<?> container, 
                                   ContainerLifecycleMode mode, String testClassName) {
        ContainerKey key = createKey(type, mode, testClassName, null);
        containers.put(key, container);
        log.info("Stored container: {}", key);
    }
    
    public static void putContainer(ContainerType type, GenericContainer<?> container, 
                                   ContainerLifecycleMode mode, String testClassName, String methodName) {
        ContainerKey key = createKey(type, mode, testClassName, methodName);
        containers.put(key, container);
        log.info("Stored container: {}", key);
    }
    
    public static GenericContainer<?> getContainer(ContainerType type, ContainerLifecycleMode mode, String testClassName) {
        ContainerKey key = createKey(type, mode, testClassName, null);
        return containers.get(key);
    }
    
    public static GenericContainer<?> getContainer(ContainerType type, ContainerLifecycleMode mode, String testClassName, String methodName) {
        ContainerKey key = createKey(type, mode, testClassName, methodName);
        return containers.get(key);
    }
    
    public static boolean containsContainer(ContainerType type, ContainerLifecycleMode mode, String testClassName) {
        ContainerKey key = createKey(type, mode, testClassName, null);
        return containers.containsKey(key);
    }
    
    public static boolean containsContainer(ContainerType type, ContainerLifecycleMode mode, String testClassName, String methodName) {
        ContainerKey key = createKey(type, mode, testClassName, methodName);
        return containers.containsKey(key);
    }
    
    public static void stopAndRemoveContainer(ContainerType type, ContainerLifecycleMode mode, String testClassName) {
        ContainerKey key = createKey(type, mode, testClassName, null);
        GenericContainer<?> container = containers.remove(key);
        if (container != null) {
            try {
                container.stop();
                log.info("Stopped and removed container: {}", key);
            } catch (Exception e) {
                log.warn("Failed to stop container: {}", key, e);
            }
        }
    }
    
    public static void stopAndRemoveContainer(ContainerType type, ContainerLifecycleMode mode, String testClassName, String methodName) {
        ContainerKey key = createKey(type, mode, testClassName, methodName);
        GenericContainer<?> container = containers.remove(key);
        if (container != null) {
            try {
                container.stop();
                log.info("Stopped and removed container: {}", key);
            } catch (Exception e) {
                log.warn("Failed to stop container: {}", key, e);
            }
        }
    }
    
    public static void stopContainers(ContainerLifecycleMode mode) {
        containers.entrySet().removeIf(entry -> {
            ContainerKey key = entry.getKey();
            if (key.getLifecycleMode() == mode) {
                try {
                    entry.getValue().stop();
                    log.info("Stopped container: {}", key);
                    return true;
                } catch (Exception e) {
                    log.warn("Failed to stop container: {}", key, e);
                }
            }
            return false;
        });
    }
    
    private static ContainerKey createKey(ContainerType type, ContainerLifecycleMode mode, String testClassName, String methodName) {
        return switch (mode) {
            case PER_CLASS -> ContainerKey.forPerClass(type, testClassName);
            case PER_METHOD -> methodName != null ? 
                ContainerKey.forPerMethod(type, testClassName, methodName) :
                ContainerKey.forPerMethod(type, testClassName);
            case REUSE -> ContainerKey.forReuse(type);
        };
    }
}

class ContainerKey {
    private final ContainerType containerType;
    private final ContainerLifecycleMode lifecycleMode;
    private final String testClassName;
    private final String methodName;
    
    private ContainerKey(ContainerType containerType, ContainerLifecycleMode lifecycleMode, String testClassName, String methodName) {
        this.containerType = containerType;
        this.lifecycleMode = lifecycleMode;
        this.testClassName = testClassName;
        this.methodName = methodName;
    }
    
    public static ContainerKey forPerClass(ContainerType type, String testClassName) {
        return new ContainerKey(type, ContainerLifecycleMode.PER_CLASS, testClassName, null);
    }
    
    public static ContainerKey forPerMethod(ContainerType type, String testClassName) {
        return new ContainerKey(type, ContainerLifecycleMode.PER_METHOD, testClassName, null);
    }
    
    public static ContainerKey forPerMethod(ContainerType type, String testClassName, String methodName) {
        return new ContainerKey(type, ContainerLifecycleMode.PER_METHOD, testClassName, methodName);
    }
    
    public static ContainerKey forReuse(ContainerType type) {
        return new ContainerKey(type, ContainerLifecycleMode.REUSE, "GLOBAL", null);
    }
    
    public ContainerLifecycleMode getLifecycleMode() {
        return lifecycleMode;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContainerKey that)) return false;
        return containerType == that.containerType && 
               lifecycleMode == that.lifecycleMode && 
               testClassName.equals(that.testClassName) &&
               java.util.Objects.equals(methodName, that.methodName);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(containerType, lifecycleMode, testClassName, methodName);
    }
    
    @Override
    public String toString() {
        return methodName != null ?
            String.format("%s-%s-%s-%s", containerType, lifecycleMode, testClassName, methodName) :
            String.format("%s-%s-%s", containerType, lifecycleMode, testClassName);
    }
}