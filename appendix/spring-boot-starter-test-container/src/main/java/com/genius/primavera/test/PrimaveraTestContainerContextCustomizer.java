package com.genius.primavera.test;

import com.genius.primavera.test.annotation.PrimaveraTestContainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PrimaveraTestContainerContextCustomizer implements ContextCustomizer {

    private final PrimaveraTestContainer annotation;

    public PrimaveraTestContainerContextCustomizer(PrimaveraTestContainer annotation) {
        this.annotation = annotation;
    }

    @Override
    public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        ConfigurableEnvironment environment = context.getEnvironment();
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("primavera.testcontainers.enabled", "true");
        properties.put("primavera.testcontainers.service.enabled", "true");
        
        // Apply annotation attributes as properties
        properties.put("primavera.testcontainers.database-name", annotation.databaseName());
        properties.put("primavera.testcontainers.username", annotation.username());
        properties.put("primavera.testcontainers.password", annotation.password());
        properties.put("primavera.testcontainers.mariadb-version", annotation.mariadbVersion());
        
        String initScript = annotation.enableInitScript() ? annotation.initScript() : "none";
        properties.put("primavera.testcontainers.init-script", initScript);
        
        MapPropertySource propertySource = new MapPropertySource("primavera-testcontainer-annotation", properties);
        environment.getPropertySources().addFirst(propertySource);
        
        log.info("Applied @PrimaveraTestContainer annotation properties: databaseName={}", annotation.databaseName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PrimaveraTestContainerContextCustomizer that = (PrimaveraTestContainerContextCustomizer) obj;
        return annotation.equals(that.annotation);
    }

    @Override
    public int hashCode() {
        return annotation.hashCode();
    }
}