package com.genius.primavera.basics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("설정 관리 테스트")
public class ConfigurationExampleTest {
    
    @Autowired
    private ConfigurationExample.ValueAnnotationExample valueExample;
    
    @Autowired
    private ConfigurationExample.AppProperties appProperties;
    
    @Test
    @DisplayName("@Value 어노테이션으로 설정값 주입")
    void testValueAnnotation() {
        assertThat(valueExample).isNotNull();
        valueExample.printConfiguration();
    }
    
    @Test
    @DisplayName("@ConfigurationProperties로 타입 안전한 설정")
    void testConfigurationProperties() {
        assertThat(appProperties).isNotNull();
        assertThat(appProperties.getName()).isEqualTo("Primavera Tutorial");
        assertThat(appProperties.getVersion()).isEqualTo("1.0.0");
        assertThat(appProperties.isDebug()).isTrue();
        assertThat(appProperties.getMaxUsers()).isEqualTo(200);
        assertThat(appProperties.getFeatures()).containsExactly("user-management", "authentication", "reporting");
        assertThat(appProperties.getDatabase()).isNotNull();
        assertThat(appProperties.getDatabase().getUrl()).isEqualTo("jdbc:h2:mem:primavera");
        assertThat(appProperties.getDatabase().getMaxConnections()).isEqualTo(20);
        assertThat(appProperties.getMetadata()).containsEntry("author", "Genius").containsEntry("environment", "development");
    }
}