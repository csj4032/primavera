package com.genius.primavera.testcontainer;

import java.util.concurrent.ConcurrentHashMap;

public class ContainerRegistry {
    
    private static final ThreadLocal<ContainerManager> MANAGERS = new ThreadLocal<>();
    private static final ConcurrentHashMap<String, Object> CLASS_LOCKS = new ConcurrentHashMap<>();
    
    public static void register(ContainerManager manager) {
        MANAGERS.set(manager);
    }
    
    public static ContainerManager get() {
        return MANAGERS.get();
    }
    
    public static void clear() {
        MANAGERS.remove();
    }
    
    public static Object getLock(String testClassName) {
        return CLASS_LOCKS.computeIfAbsent(testClassName, k -> new Object());
    }
    
    public static void removeLock(String testClassName) {
        CLASS_LOCKS.remove(testClassName);
    }
}