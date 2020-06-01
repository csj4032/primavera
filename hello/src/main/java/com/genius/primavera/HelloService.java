package com.genius.primavera;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HelloService {

    private final HelloRestClient helloRestClient;

    @HystrixCommand(fallbackMethod = "getGreetingNameFallBack")
    public String getGreetingName(String name) {
        return helloRestClient.getGreetingName(name);
    }

    private String getGreetingNameFallBack(String name) {
        log.info(name);
        return name + " (Fall Back)";
    }
}