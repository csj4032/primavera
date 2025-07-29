package com.genius.primavera.basics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
public class DependencyInjectionExample {

    public interface MessageService {
        String getMessage();
    }

    @Service("emailService")
    @Primary
    public static class EmailService implements MessageService {
        @Override
        public String getMessage() {
            return "Email 메시지";
        }
    }

    @Service("smsService")
    public static class SmsService implements MessageService {
        @Override
        public String getMessage() {
            return "SMS 메시지";
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class ConstructorInjection {
        private final MessageService messageService;

        public void sendMessage() {
            log.info("Constructor Injection: {}", messageService.getMessage());
        }
    }

    @Component
    public static class SetterInjection {
        private MessageService messageService;

        @Autowired
        public void setMessageService(MessageService messageService) {
            this.messageService = messageService;
        }

        public void sendMessage() {
            log.info("Setter Injection: {}", messageService.getMessage());
        }
    }

    @Component
    public static class FieldInjection {
        @Autowired
        private MessageService messageService;

        public void sendMessage() {
            log.info("Field Injection: {}", messageService.getMessage());
        }
    }

    @Component
    public static class QualifierInjection {
        private final MessageService smsService;
        private final MessageService emailService;

        public QualifierInjection(@Qualifier("smsService") MessageService smsService, @Qualifier("emailService") MessageService emailService) {
            this.smsService = smsService;
            this.emailService = emailService;
        }

        public void sendMessages() {
            log.info("SMS: {}", smsService.getMessage());
            log.info("Email: {}", emailService.getMessage());
        }
    }
}