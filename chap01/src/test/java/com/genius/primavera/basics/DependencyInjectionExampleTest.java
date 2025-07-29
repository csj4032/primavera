package com.genius.primavera.basics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
public class DependencyInjectionExampleTest {

    @Autowired
    private DependencyInjectionExample.ConstructorInjection constructorInjection;

    @Autowired
    private DependencyInjectionExample.SetterInjection setterInjection;

    @Autowired
    private DependencyInjectionExample.FieldInjection fieldInjection;

    @Autowired
    private DependencyInjectionExample.QualifierInjection qualifierInjection;

    @MockBean(name = "emailService")
    private DependencyInjectionExample.MessageService emailService;

    @MockBean(name = "smsService")
    private DependencyInjectionExample.MessageService smsService;

    @BeforeEach
    void setup() {
        when(emailService.getMessage()).thenReturn("Mocked Email 메시지");
        when(smsService.getMessage()).thenReturn("Mocked SMS 메시지");
    }

    @Nested
    @DisplayName("ConstructorInjection Tests")
    class ConstructorInjectionTests {
        @Test
        @DisplayName("ConstructorInjection should use the primary service")
        void constructorInjectionUsesPrimaryService() {
            constructorInjection.sendMessage();
            assertThat(emailService.getMessage()).isEqualTo("Mocked Email 메시지");
        }
    }

    @Nested
    @DisplayName("SetterInjection Tests")
    class SetterInjectionTests {
        @Test
        @DisplayName("SetterInjection should use the primary service")
        void setterInjectionUsesPrimaryService() {
            setterInjection.sendMessage();
            assertThat(emailService.getMessage()).isEqualTo("Mocked Email 메시지");
        }
    }

    @Nested
    @DisplayName("FieldInjection Tests")
    class FieldInjectionTests {
        @Test
        @DisplayName("FieldInjection should use the primary service")
        void fieldInjectionUsesPrimaryService() {
            fieldInjection.sendMessage();
            assertThat(emailService.getMessage()).isEqualTo("Mocked Email 메시지");
        }
    }

    @Nested
    @DisplayName("QualifierInjection Tests")
    class QualifierInjectionTests {
        @Test
        @DisplayName("QualifierInjection should use the correct services")
        void qualifierInjectionUsesCorrectServices() {
            qualifierInjection.sendMessages();
            assertThat(smsService.getMessage()).isEqualTo("Mocked SMS 메시지");
            assertThat(emailService.getMessage()).isEqualTo("Mocked Email 메시지");
        }
    }
}