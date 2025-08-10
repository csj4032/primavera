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
    @DisplayName("@SpringBootConfigurationshould connection file connection test file test")
    void shouldHaveExactlyOneSpringBootConfiguration() {
        String[] configBeanNames = applicationContext.getBeanNamesForAnnotation(SpringBootConfiguration.class);
        assertThat(configBeanNames).hasSize(1);
        assertThat(configBeanNames[0]).isEqualTo("springBootStarterApplication");
    }

    @Test
    @DisplayName("@SpringBootConfigurationshould connection file @Configuration connection test")
    void shouldHaveConfigurationCharacteristics() {
        String[] configBeanNames = applicationContext.getBeanNamesForAnnotation(Configuration.class);
        String[] springBootConfigBeanNames = applicationContext.getBeanNamesForAnnotation(SpringBootConfiguration.class);
        assertThat(configBeanNames).contains(springBootConfigBeanNames[0]);
    }

    @Test
    @DisplayName("@SpringBootConfigurationshould proxyBeanMethods should test file test")
    void shouldRespectProxyBeanMethodsAttribute() {
        String configClassName = applicationContext.getBeanNamesForAnnotation(SpringBootConfiguration.class)[0];
        Object configBean = applicationContext.getBean(configClassName);
        assertThat(configBean.getClass().getName()).contains("$$");
    }

    @Test
    @DisplayName("test should file needs to be added registeredshould test")
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
    @DisplayName("test @Configuration file @SpringBootConfigurationshould test file test")
    void shouldWorkWithNestedConfigurations() {
        boolean hasInnerConfigBean = applicationContext.containsBean("innerConfigBean");
        assertThat(hasInnerConfigBean).isTrue();
    }

    @Test
    @DisplayName("@SpringBootConfigurationshould file needs to be added should test")
    void shouldBeComponentScanningStartingPoint() {
        boolean hasWorldServiceImpl = applicationContext.containsBean("worldServiceImpl");
        boolean hasHelloServiceImpl = applicationContext.containsBean("helloServiceImpl");
        assertThat(hasWorldServiceImpl).isTrue();
        assertThat(hasHelloServiceImpl).isTrue();
    }

    @Test
    @DisplayName("@SpringBootConfigurationshould @Configurationshould test annotationtest should test")
    void shouldHaveConfigurationAsMetaAnnotation() {
        Class<SpringBootConfiguration> annotationClass = SpringBootConfiguration.class;
        boolean hasConfigurationAnnotation = annotationClass.isAnnotationPresent(Configuration.class);
        assertThat(hasConfigurationAnnotation).isTrue();
    }

    @Test
    @DisplayName("@SpringBootConfigurationshould connection file test should @Bean file should test")
    void shouldCacheBeanMethods() {
        String configClassName = applicationContext.getBeanNamesForAnnotation(SpringBootConfiguration.class)[0];
        Object configBean = applicationContext.getBean(configClassName);
        assertThat(configBean.getClass().getName()).contains("$$SpringCGLIB$$");
    }
}