package com.genius.primavera.testcontainer;

public enum ContainerLifecycleMode {
    /**
     * 컨테이너를 여러 테스트 간에 재사용 (성능 최적화)
     */
    REUSE,
    
    /**
     * 각 테스트마다 독립적인 컨테이너 생성 (완전한 격리)
     */
    PER_TEST,
    
    /**
     * 각 테스트 클래스마다 독립적인 컨테이너 생성
     */
    PER_CLASS
}