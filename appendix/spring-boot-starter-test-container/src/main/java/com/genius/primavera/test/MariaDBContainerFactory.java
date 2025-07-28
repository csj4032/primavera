package com.genius.primavera.test;

import com.genius.primavera.test.annotation.PrimaveraTestContainer;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.MariaDBContainer;

import java.lang.reflect.Method;

@Slf4j
public class MariaDBContainerFactory {

    public static MariaDBContainer<?> createFromTestClass() {
        Class<?> testClass = findTestClass();
        if (testClass != null) {
            log.info("Found test class: {}", testClass.getName());
            PrimaveraTestContainer annotation = findAnnotation(testClass);
            if (annotation != null) {
                log.info("Found @PrimaveraTestContainer annotation with databaseName: {}", annotation.databaseName());
                return createContainer(annotation);
            }
        }
        log.info("Using default container configuration");
        return createDefaultContainer();
    }

    private static Class<?> findTestClass() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        log.debug("Searching for test class in stack trace:");
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            log.debug("  Checking: {}", className);
            
            // 테스트 클래스 패턴 확인
            if ((className.contains(".test.") || className.contains(".dataSource.")) && 
                (className.endsWith("Test") || className.endsWith("Tests"))) {
                try {
                    Class<?> clazz = Class.forName(className);
                    // @PrimaveraTestContainer 어노테이션 확인
                    if (clazz.isAnnotationPresent(PrimaveraTestContainer.class)) {
                        log.debug("Found test class with @PrimaveraTestContainer: {}", className);
                        return clazz;
                    }
                    // 테스트 메소드 확인
                    Method[] methods = clazz.getDeclaredMethods();
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(org.junit.jupiter.api.Test.class)) {
                            log.debug("Found test class with @Test methods: {}", className);
                            return clazz;
                        }
                    }
                } catch (ClassNotFoundException e) {
                    log.debug("Could not load test class: {}", className);
                }
            }
        }
        log.debug("No test class found in stack trace");
        return null;
    }

    private static PrimaveraTestContainer findAnnotation(Class<?> testClass) {
        PrimaveraTestContainer annotation = testClass.getAnnotation(PrimaveraTestContainer.class);
        if (annotation != null) {
            return annotation;
        }

        Class<?> superClass = testClass.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            annotation = superClass.getAnnotation(PrimaveraTestContainer.class);
            if (annotation != null) {
                return annotation;
            }
            superClass = superClass.getSuperclass();
        }
        return null;
    }

    private static MariaDBContainer<?> createContainer(PrimaveraTestContainer annotation) {
        MariaDBContainer<?> container = new MariaDBContainer<>(annotation.mariadbVersion())
                .withDatabaseName(annotation.databaseName())
                .withUsername(annotation.username())
                .withPassword(annotation.password())
                .withCommand("--default-authentication-plugin=mysql_native_password");

        if (annotation.enableInitScript() && !annotation.initScript().isEmpty() && !"none".equals(annotation.initScript())) {
            container.withInitScript(annotation.initScript());
        }

        return container;
    }

    private static MariaDBContainer<?> createDefaultContainer() {
        return new MariaDBContainer<>("mariadb:11.4.7")
                .withDatabaseName("primavera")
                .withUsername("primavera")
                .withPassword("primavera")
                .withInitScript("sql/schema.sql")
                .withCommand("--default-authentication-plugin=mysql_native_password");
    }
}