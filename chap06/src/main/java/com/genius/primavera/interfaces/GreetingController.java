package com.genius.primavera.interfaces;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GreetingController {

    @GetMapping(value = "/index")
    public String index(Model model) {
        return "index";
    }
}