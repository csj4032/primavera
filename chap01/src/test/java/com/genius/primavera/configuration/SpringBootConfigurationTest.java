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
    @DisplayName("@SpringBootConfigurationtranslated_text_1 translated_text_3 translated_text_4 translated_text_3 translated_text_2 translated_text_4 translated_text_2")
    void shouldHaveExactlyOneSpringBootConfiguration() {
        String[] configBeanNames = applicationContext.getBeanNamesForAnnotation(SpringBootConfiguration.class);
        assertThat(configBeanNames).hasSize(1);
        assertThat(configBeanNames[0]).isEqualTo("springBootStarterApplication");
    }

    @Test
    @DisplayName("@SpringBootConfigurationtranslated_text_1 translated_text_3 translated_text_4 @Configuration translated_text_3 translated_text_3 translated_text_2")
    void shouldHaveConfigurationCharacteristics() {
        String[] configBeanNames = applicationContext.getBeanNamesForAnnotation(Configuration.class);
        String[] springBootConfigBeanNames = applicationContext.getBeanNamesForAnnotation(SpringBootConfiguration.class);
        assertThat(configBeanNames).contains(springBootConfigBeanNames[0]);
    }

    @Test
    @DisplayName("@SpringBootConfigurationtranslated_text_1 proxyBeanMethods translated_text_1 translated_text_2 translated_text_4 translated_text_2")
    void shouldRespectProxyBeanMethodsAttribute() {
        String configClassName = applicationContext.getBeanNamesForAnnotation(SpringBootConfiguration.class)[0];
        Object configBean = applicationContext.getBean(configClassName);
        assertThat(configBean.getClass().getName()).contains("$$");
    }

    @Test
    @DisplayName("translated_text_2 translated_text_1 translated_text_4 translated_text_1 translated_text_1 registeredtranslated_text_1 translated_text_2")
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
    @DisplayName("translated_text_2 @Configuration translated_text_4 @SpringBootConfigurationtranslated_text_1 translated_text_2 translated_text_4 translated_text_2")
    void shouldWorkWithNestedConfigurations() {
        boolean hasInnerConfigBean = applicationContext.containsBean("innerConfigBean");
        assertThat(hasInnerConfigBean).isTrue();
    }

    @Test
    @DisplayName("@SpringBootConfigurationtranslated_text_1 translated_text_4 translated_text_1 translated_text_1 translated_text_1 translated_text_2")
    void shouldBeComponentScanningStartingPoint() {
        boolean hasWorldServiceImpl = applicationContext.containsBean("worldServiceImpl");
        boolean hasHelloServiceImpl = applicationContext.containsBean("helloServiceImpl");
        assertThat(hasWorldServiceImpl).isTrue();
        assertThat(hasHelloServiceImpl).isTrue();
    }

    @Test
    @DisplayName("@SpringBootConfigurationtranslated_text_1 @Configurationtranslated_text_1 translated_text_2 annotationtranslated_text_2 translated_text_1 translated_text_2")
    void shouldHaveConfigurationAsMetaAnnotation() {
        Class<SpringBootConfiguration> annotationClass = SpringBootConfiguration.class;
        boolean hasConfigurationAnnotation = annotationClass.isAnnotationPresent(Configuration.class);
        assertThat(hasConfigurationAnnotation).isTrue();
    }

    @Test
    @DisplayName("@SpringBootConfigurationtranslated_text_1 translated_text_3 translated_text_4 translated_text_2 translated_text_1 @Bean translated_text_4 translated_text_1 translated_text_2")
    void shouldCacheBeanMethods() {
        String configClassName = applicationContext.getBeanNamesForAnnotation(SpringBootConfiguration.class)[0];
        Object configBean = applicationContext.getBean(configClassName);
        assertThat(configBean.getClass().getName()).contains("$$SpringCGLIB$$");
    }
}