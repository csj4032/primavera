package com.genius.primavera.autoconfigure;

import org.junit.jupiter.api.*;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Lucy Filter 프로퍼티 설정 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LucyFilterDelegatingPropertiesTest {

    @Test
    @Order(1)
    @DisplayName("기본 설정값이 올바르게 설정된다")
    void shouldHaveDefaultValues() {
        LucyFilterDelegatingProperties properties = new LucyFilterDelegatingProperties();
        
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getName()).isEqualTo("xssEscapeServletFilter");
        assertThat(properties.getOrder()).isEqualTo(1);
        assertThat(properties.getAddUrlPatterns()).containsExactly("/*");
    }

    @Test
    @Order(2)
    @DisplayName("프로퍼티 바인딩이 올바르게 동작한다")
    void shouldBindPropertiesCorrectly() {
        Map<String, Object> map = new HashMap<>();
        map.put("spring.lucy-filter.enabled", "false");
        map.put("spring.lucy-filter.name", "customFilter");
        map.put("spring.lucy-filter.order", "100");
        map.put("spring.lucy-filter.add-url-patterns", "/api/*,/admin/*");
        
        ConfigurationPropertySource source = new MapConfigurationPropertySource(map);
        Binder binder = new Binder(source);
        
        LucyFilterDelegatingProperties properties = binder
                .bind("spring.lucy-filter", LucyFilterDelegatingProperties.class)
                .get();
        
        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getName()).isEqualTo("customFilter");
        assertThat(properties.getOrder()).isEqualTo(100);
        assertThat(properties.getAddUrlPatterns()).containsExactly("/api/*", "/admin/*");
    }

    @Test
    @Order(3)
    @DisplayName("URL 패턴 배열이 올바르게 파싱된다")
    void shouldParseUrlPatternsArray() {
        Map<String, Object> map = new HashMap<>();
        map.put("spring.lucy-filter.add-url-patterns[0]", "/api/*");
        map.put("spring.lucy-filter.add-url-patterns[1]", "/admin/*");
        map.put("spring.lucy-filter.add-url-patterns[2]", "/user/*");
        
        ConfigurationPropertySource source = new MapConfigurationPropertySource(map);
        Binder binder = new Binder(source);
        
        LucyFilterDelegatingProperties properties = binder
                .bind("spring.lucy-filter", LucyFilterDelegatingProperties.class)
                .get();
        
        assertThat(properties.getAddUrlPatterns())
                .hasSize(3)
                .containsExactly("/api/*", "/admin/*", "/user/*");
    }

    @Test
    @Order(4)
    @DisplayName("부분적인 프로퍼티 설정시 나머지는 기본값을 유지한다")
    void shouldKeepDefaultsForUnsetProperties() {
        Map<String, Object> map = new HashMap<>();
        map.put("spring.lucy-filter.name", "myFilter");
        
        ConfigurationPropertySource source = new MapConfigurationPropertySource(map);
        Binder binder = new Binder(source);
        
        LucyFilterDelegatingProperties properties = binder
                .bind("spring.lucy-filter", LucyFilterDelegatingProperties.class)
                .orElseGet(LucyFilterDelegatingProperties::new);
        
        assertThat(properties.getName()).isEqualTo("myFilter");
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getOrder()).isEqualTo(1);
        assertThat(properties.getAddUrlPatterns()).containsExactly("/*");
    }

    @Test
    @Order(5)
    @DisplayName("setter를 통한 프로퍼티 수정이 가능하다")
    void shouldAllowPropertyModification() {
        LucyFilterDelegatingProperties properties = new LucyFilterDelegatingProperties();
        
        properties.setEnabled(false);
        properties.setName("modifiedFilter");
        properties.setOrder(50);
        properties.setAddUrlPatterns(new String[]{"/custom/*", "/special/*"});
        
        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getName()).isEqualTo("modifiedFilter");
        assertThat(properties.getOrder()).isEqualTo(50);
        assertThat(properties.getAddUrlPatterns()).containsExactly("/custom/*", "/special/*");
    }
}