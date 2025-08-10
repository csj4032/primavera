package com.genius.primavera.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class PrimaveraPropertiesTest {

    @Autowired
    private PrimaveraProperties properties;

    @Test
    @DisplayName("test connection file with validation")
    void basicPropertiesBinding() {
        String databaseUsername = properties.getDatabase().getUsername();
        String databaseUrl = properties.getDatabase().getUrl();
        List<String> tables = properties.getDatabase().getTables();
        assertThat(databaseUsername).isEqualTo("test_user");
        assertThat(databaseUrl).isEqualTo("jdbc:mariadb://localhost:3308/primavera?serverTimezone=UTC");
        assertThat(tables).containsExactly("user", "role");
    }

    @Test
    @DisplayName("connection file with validation")
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
    @DisplayName("connection test connection file with validation")
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
    @DisplayName("processing test with Endpoint validation")
    void profileSpecificPropertyOverride() {
        PrimaveraProperties.Cache cache = properties.getCache();
        assertThat(cache.isEnabled()).isTrue();
        assertThat(cache.getTimeToLive()).isEqualTo(Duration.ofMinutes(5));
        assertThat(cache.getMaxEntries()).isEqualTo(1000);
    }

    @Test
    @DisplayName("test connection(kebab-case) test connection(camelCase)should Endpoint validation")
    void kebabCaseToCamelCaseConversion() {
        PrimaveraProperties.Search.Params searchParams = properties.getSearch().getParams();
        assertThat(searchParams.getEnableHighlight()).isTrue();
    }

    @Test
    @DisplayName("Map test connection file with validation")
    void mapPropertyBinding() {
        Map<String, String> features = properties.getFeatures();
        assertThat(features).containsEntry("darkMode", "enabled");
        assertThat(features).containsEntry("betaFeatures", "disabled");
        assertThat(features).containsEntry("maxUploadSize", "10MB");
    }

    @Test
    @DisplayName("Duration test connection file with validation")
    void durationPropertyBinding() {
        Duration timeToLive = properties.getCache().getTimeToLive();
        assertThat(timeToLive).isEqualTo(Duration.ofMinutes(5));
    }
}