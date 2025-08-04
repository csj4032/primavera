package com.genius.primavera.application.injection;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@DisplayName("EmailService Test")
public class EmailServiceTest {

    @Test
    @DisplayName("EmailService should send email successfully")
    public void testSendEmail() {
        EmailService emailService = new EmailServiceImpl(new LoggingServiceImpl());
        String result = emailService.sendEmail();
        log.info("EmailService result: {}", result);
        assertNotNull(result);
    }

    @Test
    @DisplayName("EmailService should not be null")
    public void testEmailServiceNotNull() {
        EmailService emailService = new EmailServiceImpl(new LoggingServiceImpl());
        assertNotNull(emailService, "EmailService should not be null");
        log.info("EmailService is not null");
    }
}