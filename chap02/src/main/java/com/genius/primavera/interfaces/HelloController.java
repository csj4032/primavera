package com.genius.primavera.interfaces;

import com.genius.primavera.applicaiton.HelloService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HelloController {

	private final HelloService helloService;

	@GetMapping("/hello")
	public String helloWorld(Model model) {
		model.addAttribute("hello", helloService.getUsers());
		return "hello";
	}
}
