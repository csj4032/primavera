package com.genius.primavera.application.injection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final LoggingService loggingService;

    @Override
    public String sendEmail() {
        loggingService.logMessage();
        return "Email sent successfully";
    }
}
