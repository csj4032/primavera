package com.genius.primavera.testContainer;

import java.util.Set;

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

    public record TestContext(String testClassName, Set<ContainerType> containerTypes, ContainerLifecycleMode lifecycleMode) {
    }
}