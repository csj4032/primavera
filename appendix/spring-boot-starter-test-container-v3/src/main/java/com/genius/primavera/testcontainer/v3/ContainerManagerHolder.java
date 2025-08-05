package com.genius.primavera.testcontainer.v3;

/**
 * ContainerManager를 ThreadLocal로 관리하는 홀더 클래스
 * 
 * <p>JUnit Extension과 Spring Context Initializer 간 통신을 위해 사용됩니다.</p>
 */
public class ContainerManagerHolder {
    
    private static final ThreadLocal<ContainerManager> HOLDER = new ThreadLocal<>();
    
    /**
     * 현재 스레드의 ContainerManager 설정
     */
    public static void set(ContainerManager containerManager) {
        HOLDER.set(containerManager);
    }
    
    /**
     * 현재 스레드의 ContainerManager 조회
     */
    public static ContainerManager get() {
        return HOLDER.get();
    }
    
    /**
     * 현재 스레드의 ContainerManager 정리
     */
    public static void clear() {
        HOLDER.remove();
    }
}