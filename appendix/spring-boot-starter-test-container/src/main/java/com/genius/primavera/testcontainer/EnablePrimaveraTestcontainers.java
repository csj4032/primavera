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

    /**
     * 사용할 컨테이너 타입들을 지정합니다.
     * application.yml에 설정이 있으면 그 설정을 사용하고, 없으면 기본값으로 컨테이너를 생성합니다.
     */
    ContainerType[] containers() default {ContainerType.MARIADB};

    /**
     * 컨테이너 라이프사이클 모드를 지정합니다.
     */
    ContainerLifecycleMode lifecycleMode() default ContainerLifecycleMode.PER_CLASS;
}