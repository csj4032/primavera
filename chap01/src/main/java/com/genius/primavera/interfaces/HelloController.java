package com.genius.primavera.interfaces;

import com.genius.primavera.application.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/")
public class HelloController {

	@Autowired
	private HelloService helloService;

	@GetMapping
	public String index() {
		return "index";
	}

	@GetMapping(value = "articles")
	public @ResponseBody
	List<String> articles() {
		return helloService.getArticles();
	}
}
