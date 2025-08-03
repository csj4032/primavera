package com.genius.primavera.testContainer;

import java.util.Set;

/**
 * 테스트 컨텍스트 정보를 저장하는 ThreadLocal 홀더
 */
public class TestContextHolder {
    
    private static final ThreadLocal<TestContext> CONTEXT_HOLDER = new ThreadLocal<>();
    
    public static void setContext(String testClassName, Set<ContainerType> containerTypes, ContainerLifecycleMode lifecycleMode) {
        CONTEXT_HOLDER.set(new TestContext(testClassName, containerTypes, lifecycleMode));
    }
    
    public static TestContext getContext() {
        return CONTEXT_HOLDER.get();
    }
    
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }
    
    public static class TestContext {
        private final String testClassName;
        private final Set<ContainerType> containerTypes;
        private final ContainerLifecycleMode lifecycleMode;
        
        public TestContext(String testClassName, Set<ContainerType> containerTypes, ContainerLifecycleMode lifecycleMode) {
            this.testClassName = testClassName;
            this.containerTypes = containerTypes;
            this.lifecycleMode = lifecycleMode;
        }
        
        public String getTestClassName() {
            return testClassName;
        }
        
        public Set<ContainerType> getContainerTypes() {
            return containerTypes;
        }
        
        public ContainerLifecycleMode getLifecycleMode() {
            return lifecycleMode;
        }
    }
}