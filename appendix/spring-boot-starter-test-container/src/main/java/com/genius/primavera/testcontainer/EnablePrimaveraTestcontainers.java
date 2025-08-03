package com.genius.primavera.testContainer;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(TestContainerLifecycleExtension.class)
@Import(PrimaveraTestcontainersConfiguration.class)
@ContextConfiguration(initializers = PrimaveraTestcontainersInitializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public @interface EnablePrimaveraTestcontainers {

    ContainerType[] containers() default {ContainerType.MARIADB};

    ContainerLifecycleMode lifecycleMode() default ContainerLifecycleMode.PER_CLASS;
}