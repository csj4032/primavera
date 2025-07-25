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
        // given - 스프링이 application.yml의 속성을 PrimaveraProperties에 바인딩

        // when - PrimaveraProperties 객체에서 속성값을 조회
        String databaseUsername = properties.getDatabase().getUsername();
        String databaseUrl = properties.getDatabase().getUrl();
        List<String> tables = properties.getDatabase().getTables();

        // then - 값이 정확히 바인딩되었는지 검증
        assertThat(databaseUsername).isEqualTo("test_user");
        assertThat(databaseUrl).isEqualTo("jdbc:h2:mem:testdb");
        assertThat(tables).containsExactly("user", "role");
    }

    @Test
    @DisplayName("중첩된 객체가 올바르게 바인딩되는지 검증")
    void nestedObjectBinding() {
        // given
        PrimaveraProperties.Search search = properties.getSearch();

        // when
        String keyword = search.getParams().getKeyword();
        Integer page = search.getParams().getPage();
        String sort = search.getParams().getSort();

        // then
        assertThat(keyword).isEqualTo("spring");
        assertThat(page).isEqualTo(1);
        assertThat(sort).isEqualTo("desc");
    }

    @Test
    @DisplayName("리스트 타입 속성이 올바르게 바인딩되는지 검증")
    void listPropertyBinding() {
        // given
        List<PrimaveraProperties.User> users = properties.getUsers();

        // when & then
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
        // given
        PrimaveraProperties.Cache cache = properties.getCache();

        // when & then
        // test 프로파일에서 cache.enabled = false로 오버라이딩됨
        assertThat(cache.isEnabled()).isFalse();

        // 오버라이딩되지 않은 기본값 유지
        assertThat(cache.getTimeToLive()).isEqualTo(Duration.ofMinutes(10));
        assertThat(cache.getMaxEntries()).isEqualTo(2000);
    }

    @Test
    @DisplayName("케밥 케이스(kebab-case)��서 카멜 케이스(camelCase)로 변환되는지 검증")
    void kebabCaseToCamelCaseConversion() {
        // given
        PrimaveraProperties.Search.Params searchParams = properties.getSearch().getParams();

        // when & then
        // YAML에서는 enable-highlight 형태로 정의되었지만 Java에서는 enableHighlight 속성으로 바인딩됨
        assertThat(searchParams.getEnableHighlight()).isTrue();
    }

    @Test
    @DisplayName("Map 타입 속성이 올바르게 바인딩되는지 검증")
    void mapPropertyBinding() {
        // given
        Map<String, String> features = properties.getFeatures();

        // when & then
        assertThat(features).containsEntry("darkMode", "enabled");
        assertThat(features).containsEntry("betaFeatures", "disabled");
        assertThat(features).containsEntry("maxUploadSize", "10MB");
    }

    @Test
    @DisplayName("Duration 타입 속성이 올바르게 바인딩되는지 검증")
    void durationPropertyBinding() {
        // given
        Duration timeToLive = properties.getCache().getTimeToLive();

        // when & then
        assertThat(timeToLive).isEqualTo(Duration.ofMinutes(10));
    }
}
