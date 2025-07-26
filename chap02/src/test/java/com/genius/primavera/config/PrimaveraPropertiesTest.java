package com.genius.primavera.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class PrimaveraPropertiesTest {

    @Autowired
    private PrimaveraProperties properties;

    @Test
    @DisplayName("기본 속성이 올바르게 바인딩되는지 검증")
    void basicPropertiesBinding() {
        String databaseUsername = properties.getDatabase().getUsername();
        String databaseUrl = properties.getDatabase().getUrl();
        List<String> tables = properties.getDatabase().getTables();
        assertThat(databaseUsername).isEqualTo("test_user");
        assertThat(databaseUrl).isEqualTo("jdbc:mysql://localhost:1109/primavera");
        assertThat(tables).containsExactly("user", "role");
    }

    @Test
    @DisplayName("중첩된 객체가 올바르게 바인딩되는지 검증")
    void nestedObjectBinding() {
        PrimaveraProperties.Search search = properties.getSearch();
        String keyword = search.getParams().getKeyword();
        Integer page = search.getParams().getPage();
        String sort = search.getParams().getSort();
        assertThat(keyword).isEqualTo("spring");
        assertThat(page).isEqualTo(1);
        assertThat(sort).isEqualTo("desc");
    }

    @Test
    @DisplayName("리스트 타입 속성이 올바르게 바인딩되는지 검증")
    void listPropertyBinding() {
        List<PrimaveraProperties.User> users = properties.getUsers();
        assertThat(users).hasSize(2);
        PrimaveraProperties.User adminUser = users.get(0);
        assertThat(adminUser.getId()).isEqualTo(1);
        assertThat(adminUser.getEmail()).isEqualTo("admin@primavera.com");
        assertThat(adminUser.getRole()).isEqualTo("ADMIN");
        assertThat(adminUser.isActive()).isTrue();
        PrimaveraProperties.User normalUser = users.get(1);
        assertThat(normalUser.getEmail()).isEqualTo("user@primavera.com");
        assertThat(normalUser.getRole()).isEqualTo("USER");
    }

    @Test
    @DisplayName("프로파일별 속성 오버라이딩이 작동하는지 검증")
    void profileSpecificPropertyOverride() {
        PrimaveraProperties.Cache cache = properties.getCache();
        assertThat(cache.isEnabled()).isFalse();
        assertThat(cache.getTimeToLive()).isEqualTo(Duration.ofMinutes(10));
        assertThat(cache.getMaxEntries()).isEqualTo(2000);
    }

    @Test
    @DisplayName("케밥 케이스(kebab-case) 카멜 케이스(camelCase)로 변환되는지 검증")
    void kebabCaseToCamelCaseConversion() {
        PrimaveraProperties.Search.Params searchParams = properties.getSearch().getParams();
        assertThat(searchParams.getEnableHighlight()).isTrue();
    }

    @Test
    @DisplayName("Map 타입 속성이 올바르게 바인딩되는지 검증")
    void mapPropertyBinding() {
        Map<String, String> features = properties.getFeatures();
        assertThat(features).containsEntry("darkMode", "enabled");
        assertThat(features).containsEntry("betaFeatures", "disabled");
        assertThat(features).containsEntry("maxUploadSize", "10MB");
    }

    @Test
    @DisplayName("Duration 타입 속성이 올바르게 바인딩되는지 검증")
    void durationPropertyBinding() {
        Duration timeToLive = properties.getCache().getTimeToLive();
        assertThat(timeToLive).isEqualTo(Duration.ofMinutes(10));
    }
}