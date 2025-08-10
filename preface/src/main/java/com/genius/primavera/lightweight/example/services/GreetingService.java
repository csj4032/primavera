package com.genius.primavera.lightweight.example.services;

import com.genius.primavera.lightweight.annotations.PrimaveraComponent;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@PrimaveraComponent
public class GreetingService {
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String sayHello(String name) {
        String greeting = String.format(" Endpoint, %sshould! Primaverashould test Endpoint!", name);
        log.info("connection creation: {}", greeting);
        return greeting;
    }

    public String sayHelloWithTime(String name) {
        String currentTime = LocalDateTime.now().format(formatter);
        String greeting = String.format(" Endpoint, %sshould! test connection %sconnection.", name, currentTime);
        log.info("test connection creation: {}", greeting);
        return greeting;
    }

    public String sayGoodbye(String name) {
        String goodbye = String.format(" connection, %sshould! needs to be added Endpoint!", name);
        log.info("test creation: {}", goodbye);
        return goodbye;
    }
}