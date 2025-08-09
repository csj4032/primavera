package com.genius.primavera.configuration;

import com.genius.primavera.SpringBootStarterApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {SpringBootStarterApplication.class, SpringBootConfigurationTest.InnerConfiguration.class})
@ActiveProfiles("test")
public class SpringBootConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("@SpringBootConfiguration이 적용된 클래스가 정확히 하나 존재해야 한다")
    void shouldHaveExactlyOneSpringBootConfiguration() {
        String[] configBeanNames = applicationContext.getBeanNamesForAnnotation(SpringBootConfiguration.class);
        assertThat(configBeanNames).hasSize(1);
        assertThat(configBeanNames[0]).isEqualTo("springBootStarterApplication");
    }

    @Test
    @DisplayName("@SpringBootConfiguration이 적용된 클래스는 @Configuration 특성을 가져야 한다")
    void shouldHaveConfigurationCharacteristics() {
        String[] configBeanNames = applicationContext.getBeanNamesForAnnotation(Configuration.class);
        String[] springBootConfigBeanNames = applicationContext.getBeanNamesForAnnotation(SpringBootConfiguration.class);
        assertThat(configBeanNames).contains(springBootConfigBeanNames[0]);
    }

    @Test
    @DisplayName("@SpringBootConfiguration의 proxyBeanMethods 속성이 정상 동작해야 한다")
    void shouldRespectProxyBeanMethodsAttribute() {
        String configClassName = applicationContext.getBeanNamesForAnnotation(SpringBootConfiguration.class)[0];
        Object configBean = applicationContext.getBean(configClassName);
        assertThat(configBean.getClass().getName()).contains("$$");
    }

    @Test
    @DisplayName("메인 애플리케이션 클래스에 정의된 빈들이 등록되어야 한다")
    void shouldRegisterBeansDefinedInConfiguration() {
        boolean hasApplicationRunner = applicationContext.containsBean("applicationRunner");
        boolean hasCommandLineRunner = applicationContext.containsBean("commandLineRunner");
        assertThat(hasApplicationRunner).isTrue();
        assertThat(hasCommandLineRunner).isTrue();
    }

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
        boolean hasInnerConfigBean = applicationContext.containsBean("innerConfigBean");
        assertThat(hasInnerConfigBean).isTrue();
    }

    @Test
    @DisplayName("@SpringBootConfiguration은 컴포넌트 스캔의 시작점이 되어야 한다")
    void shouldBeComponentScanningStartingPoint() {
        boolean hasWorldServiceImpl = applicationContext.containsBean("worldServiceImpl");
        boolean hasHelloServiceImpl = applicationContext.containsBean("helloServiceImpl");
        assertThat(hasWorldServiceImpl).isTrue();
        assertThat(hasHelloServiceImpl).isTrue();
    }

    @Test
    @DisplayName("@SpringBootConfiguration은 @Configuration을 메타 어노테이션으로 포함해야 한다")
    void shouldHaveConfigurationAsMetaAnnotation() {
        Class<SpringBootConfiguration> annotationClass = SpringBootConfiguration.class;
        boolean hasConfigurationAnnotation = annotationClass.isAnnotationPresent(Configuration.class);
        assertThat(hasConfigurationAnnotation).isTrue();
    }

    @Test
    @DisplayName("@SpringBootConfiguration이 적용된 클래스는 내부에 정의된 @Bean 메서드를 캐싱해야 한다")
    void shouldCacheBeanMethods() {
        String configClassName = applicationContext.getBeanNamesForAnnotation(SpringBootConfiguration.class)[0];
        Object configBean = applicationContext.getBean(configClassName);
        assertThat(configBean.getClass().getName()).contains("$$SpringCGLIB$$");
    }
}