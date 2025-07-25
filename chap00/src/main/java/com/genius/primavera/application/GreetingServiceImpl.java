package com.genius.primavera.application;

import org.springframework.stereotype.Service;

@Service
public class GreetingServiceImpl implements GreetingService {
    @Override
    public String hello() {
        return "Hello";
    }
}

