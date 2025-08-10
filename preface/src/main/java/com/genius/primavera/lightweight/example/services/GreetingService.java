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
        String greeting = String.format(" translated_text_5, %stranslated_text_1! Primaveratranslated_text_1 translated_text_2 translated_text_2 translated_text_5!", name);
        log.info("translated_text_3 creation: {}", greeting);
        return greeting;
    }

    public String sayHelloWithTime(String name) {
        String currentTime = LocalDateTime.now().format(formatter);
        String greeting = String.format(" translated_text_5, %stranslated_text_1! translated_text_2 translated_text_3 %stranslated_text_3.", name, currentTime);
        log.info("translated_text_2 translated_text_2 translated_text_3 creation: {}", greeting);
        return greeting;
    }

    public String sayGoodbye(String name) {
        String goodbye = String.format(" translated_text_3 translated_text_3, %stranslated_text_1! translated_text_1 translated_text_1 translated_text_5!", name);
        log.info("translated_text_2 translated_text_2 creation: {}", goodbye);
        return goodbye;
    }
}