package com.genius.primavera.interfaces;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class GreetingController {

    @GetMapping(value = "/greeting/{name}")
    public String greeting(@PathVariable(value = "name") String name) {
        return "chap13 : " + name;
    }
}