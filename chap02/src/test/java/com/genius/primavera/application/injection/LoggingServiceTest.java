package com.genius.primavera.application.injection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoggingService Test")
public class LoggingServiceTest {

    @Test
    @DisplayName("LoggingService should log message successfully")
    public void testLogMessage() {
        LoggingService loggingService = new LoggingServiceImpl();
        String result = loggingService.logMessage();
        assertNotNull(result, "LoggingService should return a non-null message");
    }

    @Test
    @DisplayName("LoggingService should not be null")
    public void testLoggingServiceNotNull() {
        LoggingService loggingService = new LoggingServiceImpl();
        assertNotNull(loggingService, "LoggingService should not be null");
    }
}