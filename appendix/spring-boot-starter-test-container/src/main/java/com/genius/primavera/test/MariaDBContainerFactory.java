package com.genius.primavera.test;

import com.genius.primavera.test.annotation.PrimaveraTestContainer;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.MariaDBContainer;

import java.lang.reflect.Method;

@Slf4j
public class MariaDBContainerFactory {

    public static MariaDBContainer<?> createFromAnnotation(Class<?> testClass) {
        PrimaveraTestContainer annotation = findAnnotation(testClass);
        if (annotation != null) return createContainer(annotation);
        return createDefaultContainer();
    }

    private static PrimaveraTestContainer findAnnotation(Class<?> testClass) {
        PrimaveraTestContainer annotation = testClass.getAnnotation(PrimaveraTestContainer.class);
        if (annotation != null) return annotation;
        Class<?> superClass = testClass.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            annotation = superClass.getAnnotation(PrimaveraTestContainer.class);
            if (annotation != null) return annotation;
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

        if (annotation.enableInitScript() && !annotation.initScript().isEmpty() && !"none".equals(annotation.initScript())) container.withInitScript(annotation.initScript());

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

    public static Class<?> findTestClass() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        for (StackTraceElement element : stackTrace) {
            try {
                Class<?> clazz = Class.forName(element.getClassName());
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(org.junit.jupiter.api.Test.class)) return clazz;
                }
            } catch (ClassNotFoundException e) {
                log.error(e.getMessage());
            }
        }

        return null;
    }
}