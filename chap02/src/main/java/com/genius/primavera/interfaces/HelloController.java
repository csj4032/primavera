package com.genius.primavera.interfaces;

import com.genius.primavera.applicaiton.HelloService;
import com.genius.primavera.applicaiton.OopsException;
import com.genius.primavera.infrastructure.aspect.PrimaveraLogging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HelloController {

	private final HelloService helloService;

	@GetMapping("/hello")
	@PrimaveraLogging(type = "Controller")
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
	public String oops(Model model) {
		if (1 == 1) throw new OopsException("oops");
		return "hello";
	}
}