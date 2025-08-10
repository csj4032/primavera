package com.genius.primavera.testcontainers;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 컨테이너 매니저의 생명주기를 관리하는 레지스트리
 * ThreadLocal을 사용하여 테스트 클래스별로 독립적인 매니저 인스턴스 관리
 */
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