package com.genius.primavera.testcontainers;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Testcontainers
@ExtendWith(TestContainerExtension.class)
@ContextConfiguration(initializers = TestContainerContextInitializer.class)
public @interface EnableTestContainers {

    TestContainer[] value() default {@TestContainer(type = ContainerType.MARIADB, name = "mariadb")};

    @Target({})
    @Retention(RetentionPolicy.RUNTIME)
    @interface TestContainer {
        ContainerType type();

        String name();
    }
}