package com.genius.primavera.application.injection;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BooServiceImpl implements BooService {

    // 생성자 순환참조 경고
    // private final FooService fooService;

    @Override
    public String boo() {
        // fooService.foo();
        return "boo";
    }
}
