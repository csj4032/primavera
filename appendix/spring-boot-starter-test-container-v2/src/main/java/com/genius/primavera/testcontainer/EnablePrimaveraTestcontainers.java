package com.genius.primavera.testcontainer;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(PrimaveraTestcontainersConfiguration.class)
@ContextConfiguration(initializers = PrimaveraTestcontainersInitializer.class)
@ExtendWith(TestContainerLifecycleExtension.class)
public @interface EnablePrimaveraTestcontainers {
    ContainerLifecycleMode lifecycleMode() default ContainerLifecycleMode.REUSE;
}