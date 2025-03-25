package com.genius.primavera.interfaces;

import com.genius.primavera.applicaiton.HelloService;
import com.genius.primavera.applicaiton.OopsException;
import com.genius.primavera.infrastructure.aspect.PrimaveraLogging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HelloController {

    private final HelloService helloService;

    @GetMapping("/hello")
    @PrimaveraLogging(type = "Controller")
    @CrossOrigin(origins = "http://localhost:8080")
    public String helloWorld(Model model) {
        model.addAttribute("hello", helloService.getUsers());
        return "hello";
    }

    @GetMapping("/hello/{id}")
    @PrimaveraLogging(type = "Controller")
    public String helloWorld(Model model, @PathVariable(name = "id") long id) {
        model.addAttribute("hello", helloService.getUserById(id));
        return "hello";
    }

    @GetMapping("/oops")
    @PrimaveraLogging(type = "Controller")
    public String oops() {
        throw new OopsException("oops");
    }

    @GetMapping("/order")
    @PrimaveraLogging(type = "Controller")
    public String order() {
        return "hello";
    }
}