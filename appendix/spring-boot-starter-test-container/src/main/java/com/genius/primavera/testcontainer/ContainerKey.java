package com.genius.primavera.testContainer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 컨테이너를 식별하기 위한 키 클래스
 * 컨테이너 타입과 라이프사이클 모드, 테스트 클래스명을 조합하여 유니크한 키를 생성
 */
@Getter
@EqualsAndHashCode
@ToString
public class ContainerKey {
    
    private final ContainerType containerType;
    private final ContainerLifecycleMode lifecycleMode;
    private final String testClassName;
    
    private ContainerKey(ContainerType containerType, ContainerLifecycleMode lifecycleMode, String testClassName) {
        this.containerType = containerType;
        this.lifecycleMode = lifecycleMode;
        this.testClassName = testClassName;
    }
    
    /**
     * REUSE 모드용 키 생성 - 모든 테스트에서 공유
     */
    public static ContainerKey forReuse(ContainerType containerType) {
        return new ContainerKey(containerType, ContainerLifecycleMode.REUSE, "GLOBAL");
    }
    
    /**
     * PER_CLASS 모드용 키 생성 - 테스트 클래스별로 고유
     */
    public static ContainerKey forPerClass(ContainerType containerType, String testClassName) {
        return new ContainerKey(containerType, ContainerLifecycleMode.PER_CLASS, testClassName);
    }
    
    /**
     * PER_TEST 모드용 키 생성 - 테스트 메서드별로 고유 (Thread 기반)
     */
    public static ContainerKey forPerTest(ContainerType containerType) {
        return new ContainerKey(containerType, ContainerLifecycleMode.PER_TEST, 
                "THREAD-" + Thread.currentThread().getId());
    }
    
    /**
     * 라이프사이클 모드에 따라 적절한 키를 생성
     */
    public static ContainerKey create(ContainerType containerType, ContainerLifecycleMode lifecycleMode, String testClassName) {
        return switch (lifecycleMode) {
            case REUSE -> forReuse(containerType);
            case PER_CLASS -> forPerClass(containerType, testClassName);
            case PER_TEST -> forPerTest(containerType);
        };
    }
    
    /**
     * 디버깅을 위한 사람이 읽기 쉬운 문자열 표현
     */
    public String getDisplayName() {
        return switch (lifecycleMode) {
            case REUSE -> containerType + "(GLOBAL)";
            case PER_CLASS -> containerType + "(" + getSimpleClassName() + ")";
            case PER_TEST -> containerType + "(" + testClassName + ")";
        };
    }
    
    private String getSimpleClassName() {
        if (testClassName == null || testClassName.isEmpty()) {
            return "UNKNOWN";
        }
        return testClassName.substring(testClassName.lastIndexOf('.') + 1);
    }
}