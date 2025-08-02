package com.genius.primavera.testcontainer;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(PrimaveraTestcontainersConfiguration.class)
@ContextConfiguration(initializers = PrimaveraTestcontainersInitializer.class)
public @interface EnablePrimaveraTestcontainers {
}