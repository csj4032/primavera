package com.genius.primavera.application;

import org.springframework.stereotype.Service;

/**
 * GreetingServiceImpl - 인사 메시지를 반환하는 서비스 구현 클래스입니다.
 * 이 클래스는 GreetingService 인터페이스를 구현하여 인사 메시지를 반환합니다.
 */
@Service
public class GreetingServiceImpl implements GreetingService {

    @Override
    public String hello() {
        return "Hello";
    }
}

