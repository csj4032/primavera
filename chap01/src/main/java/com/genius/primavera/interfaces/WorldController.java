package com.genius.primavera.interfaces;

import com.genius.primavera.application.HelloService;
import com.genius.primavera.application.WorldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
public record WorldController(HelloService helloService, WorldService worldService) {

    @GetMapping(value = "/world")
    public String world() {
        return worldService.world() + " " + helloService.hello();
    }
}
