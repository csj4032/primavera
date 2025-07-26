package com.genius.primavera.configuration;

import com.genius.primavera.SpringBootStarterApplication;
import com.genius.primavera.application.GreetingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @SpringBootConfiguration 심화 분석을 위한 테스트
 *
 * @SpringBootConfiguration은 다음과 같은 특징을 가집니다:
 * 1. 내부적으로 @Configuration을 포함하여 빈 정의의 소스임을 나타냄
 * 2. 애플리케이션당 하나만 존재해야 함 (컴포넌트 스캔 시작점)
 * 3. 테스트 환경에서 컨텍스트 로딩의 기준점으로 사용됨
 * 4. @SpringBootApplication 내부에 포함됨
 */
@SpringBootTest(classes = {SpringBootStarterApplication.class, SpringBootConfigurationTest.InnerConfiguration.class})
@ActiveProfiles("test")
public class SpringBootConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("@SpringBootConfiguration이 적용된 클래스가 정확히 하나 존재해야 한다")
    void shouldHaveExactlyOneSpringBootConfiguration() {
        // when
        String[] configBeanNames = applicationContext.getBeanNamesForAnnotation(SpringBootConfiguration.class);

        // then
        // 애플리케이션 내에 @SpringBootConfiguration이 적용된 클래스는 정확히 하나만 있어야 함
        assertThat(configBeanNames).hasSize(1);
        // 빈 이름은 일반적으로 클래스명의 camelCase 형태
        assertThat(configBeanNames[0]).isEqualTo("springBootStarterApplication");
    }

    @Test
    @DisplayName("@SpringBootConfiguration이 적용된 클래스는 @Configuration 특성을 가져야 한다")
    void shouldHaveConfigurationCharacteristics() {
        // when
        String[] configBeanNames = applicationContext.getBeanNamesForAnnotation(Configuration.class);
        String[] springBootConfigBeanNames = applicationContext.getBeanNamesForAnnotation(SpringBootConfiguration.class);

        // then
        // @SpringBootConfiguration이 적용된 빈은 @Configuration 빈 목록에도 포함되어야 함
        assertThat(configBeanNames).contains(springBootConfigBeanNames[0]);
    }

    @Test
    @DisplayName("@SpringBootConfiguration의 proxyBeanMethods 속성이 정상 동작해야 한다")
    void shouldRespectProxyBeanMethodsAttribute() {
        // given
        String configClassName = applicationContext.getBeanNamesForAnnotation(SpringBootConfiguration.class)[0];
        Object configBean = applicationContext.getBean(configClassName);

        // when & then
        // SpringBootConfiguration 클래스가 CGLIB 프록시로 생성됨 (proxyBeanMethods=true 기본값)
        assertThat(configBean.getClass().getName()).contains("$$");
    }

    @Test
    @DisplayName("메인 애플리케이션 클래스에 정의된 빈들이 등록되어야 한다")
    void shouldRegisterBeansDefinedInConfiguration() {
        // when
        boolean hasApplicationRunner = applicationContext.containsBean("applicationRunner");
        boolean hasCommandLineRunner = applicationContext.containsBean("commandLineRunner");

        // then
        // SpringBootStarterApplication에 정의된 빈들이 등록되어 있어야 함
        assertThat(hasApplicationRunner).isTrue();
        assertThat(hasCommandLineRunner).isTrue();
    }

    /**
     * 내부 @Configuration 클래스 - @SpringBootConfiguration과 함께 사용 가능
     */
    @Configuration
    static class InnerConfiguration {
        @Bean
        public String innerConfigBean() {
            return "innerConfigBean";
        }
    }

    @Test
    @DisplayName("내부 @Configuration 클래스가 @SpringBootConfiguration과 함께 동작해야 한다")
    void shouldWorkWithNestedConfigurations() {
        // when
        boolean hasInnerConfigBean = applicationContext.containsBean("innerConfigBean");

        // then
        // 내부 @Configuration 클래스에 정의된 빈도 등록되어야 함
        assertThat(hasInnerConfigBean).isTrue();
    }

    @Test
    @DisplayName("@SpringBootConfiguration은 컴포넌트 스캔의 시작점이 되어야 한다")
    void shouldBeComponentScanningStartingPoint() {
        // when
        // 컴포넌트 스캔으로 등록된 서비스 빈 확인
        boolean hasWorldServiceImpl = applicationContext.containsBean("worldServiceImpl");
        boolean hasGreetingServiceImpl = applicationContext.containsBean("greetingServiceImpl");

        // then
        // @Service 등으로 표시된 클래스들이 컴포넌트 스캔으로 발견되어야 함
        assertThat(hasWorldServiceImpl).isTrue();
        assertThat(hasGreetingServiceImpl).isTrue();
    }

    /**
     * 추가 테스트: SpringBootConfiguration의 메타 어노테이션 분석
     */
    @Test
    @DisplayName("@SpringBootConfiguration은 @Configuration을 메타 어노테이션으로 포함해야 한다")
    void shouldHaveConfigurationAsMetaAnnotation() {
        // given
        Class<SpringBootConfiguration> annotationClass = SpringBootConfiguration.class;

        // when
        boolean hasConfigurationAnnotation = annotationClass.isAnnotationPresent(Configuration.class);

        // then
        assertThat(hasConfigurationAnnotation).isTrue();
    }

    @Test
    @DisplayName("@SpringBootConfiguration이 적용된 클래스는 내부에 정의된 @Bean 메서드를 캐싱해야 한다")
    void shouldCacheBeanMethods() {
        // given
        String configClassName = applicationContext.getBeanNamesForAnnotation(SpringBootConfiguration.class)[0];
        Object configBean = applicationContext.getBean(configClassName);

        // when & then
        // 직접 테스트하기는 어렵지만, 프록시 클래스 여부로 간접 검증
        assertThat(configBean.getClass().getName()).contains("$$SpringCGLIB$$");
    }
}
