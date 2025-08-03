package com.genius.primavera.testcontainer.v2;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;

public class TestContextCustomizer implements ContextCustomizer {
    
    private final String testClassName;
    private final String methodName;
    
    public TestContextCustomizer(String testClassName) {
        this.testClassName = testClassName;
        this.methodName = null;
    }
    
    public TestContextCustomizer(String testClassName, String methodName) {
        this.testClassName = testClassName;
        this.methodName = methodName;
    }
    
    @Override
    public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        // 테스트 클래스별 또는 메서드별로 고유한 컨텍스트 생성을 위한 커스터마이저
        context.getBeanFactory().registerSingleton("testClassName", testClassName);
        if (methodName != null) {
            context.getBeanFactory().registerSingleton("testMethodName", methodName);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TestContextCustomizer)) return false;
        TestContextCustomizer that = (TestContextCustomizer) obj;
        return testClassName.equals(that.testClassName) && 
               java.util.Objects.equals(methodName, that.methodName);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(testClassName, methodName);
    }
}