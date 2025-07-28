package com.genius.primavera.test;

import com.genius.primavera.test.annotation.PrimaveraTestContainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PrimaveraTestContainerBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private boolean propertiesSet = false;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (!propertiesSet && applicationContext != null) {
            setAnnotationProperties();
            propertiesSet = true;
        }
        return bean;
    }

    private void setAnnotationProperties() {
        // 스택 트레이스에서 테스트 클래스 찾기
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            try {
                Class<?> clazz = Class.forName(element.getClassName());
                PrimaveraTestContainer annotation = clazz.getAnnotation(PrimaveraTestContainer.class);
                if (annotation != null) {
                    applyAnnotationProperties(annotation);
                    return;
                }
            } catch (ClassNotFoundException e) {
                // 무시
            }
        }
    }

    private void applyAnnotationProperties(PrimaveraTestContainer annotation) {
        ConfigurableEnvironment environment = (ConfigurableEnvironment) applicationContext.getEnvironment();
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("primavera.testcontainers.enabled", "true");
        properties.put("primavera.testcontainers.service.enabled", "true");
        properties.put("primavera.testcontainers.database-name", annotation.databaseName());
        properties.put("primavera.testcontainers.username", annotation.username());
        properties.put("primavera.testcontainers.password", annotation.password());
        properties.put("primavera.testcontainers.mariadb-version", annotation.mariadbVersion());
        
        String initScript = annotation.enableInitScript() ? annotation.initScript() : "none";
        properties.put("primavera.testcontainers.init-script", initScript);
        
        MapPropertySource propertySource = new MapPropertySource("primavera-testcontainer-beanpostprocessor", properties);
        environment.getPropertySources().addFirst(propertySource);
        
        log.info("BeanPostProcessor에서 @PrimaveraTestContainer 속성 적용 - databaseName: {}", annotation.databaseName());
    }
}