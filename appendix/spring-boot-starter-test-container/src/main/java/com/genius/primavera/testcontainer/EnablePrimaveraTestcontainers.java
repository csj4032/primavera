package com.genius.primavera.testcontainer;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(TestContainerLifecycleExtension.class)
@Import(PrimaveraTestcontainersConfiguration.class)
@ContextConfiguration(initializers = PrimaveraTestcontainersInitializer.class)
public @interface EnablePrimaveraTestcontainers {

    ContainerLifecycleMode lifecycleMode() default ContainerLifecycleMode.PER_CLASS;

}