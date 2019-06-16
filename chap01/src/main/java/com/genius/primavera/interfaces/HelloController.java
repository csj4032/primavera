package com.genius.primavera.interfaces;

import com.genius.primavera.application.HelloService;
import com.genius.primavera.application.SpringBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/")
public class HelloController {

	private final HelloService helloService;
	private final SpringBean springBean;

	@GetMapping
	public String index(@RequestParam("number") int number) {
		log.info("bean name : " + springBean.getName() + "parameter : " + number);
		return "index";
	}

	@GetMapping(value = "articles")
	public @ResponseBody
	List<String> articles() {
		return helloService.getArticles();
	}
}
