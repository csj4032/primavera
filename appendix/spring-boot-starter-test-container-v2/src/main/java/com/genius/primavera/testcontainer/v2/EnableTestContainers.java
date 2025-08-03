package com.genius.primavera.testcontainer.v2;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(TestContainerExtension.class)
public @interface EnableTestContainers {
    
    ContainerType[] containers() default {ContainerType.MARIADB};
    
    ContainerLifecycleMode lifecycleMode() default ContainerLifecycleMode.PER_METHOD;
}