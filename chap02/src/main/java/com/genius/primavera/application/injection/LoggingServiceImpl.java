package com.genius.primavera.application.injection;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoggingServiceImpl implements LoggingService {

    @Override
    public String logMessage() {
        return "Log message recorded";
    }
}
