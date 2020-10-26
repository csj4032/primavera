package com.genius.primavera.interfaces;

import com.genius.primavera.application.IHelloService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/")
public class HelloController {

	private final IHelloService helloService;
	private final IHelloService helloServiceRequest;
	private final IHelloService helloServicePrototype;

	@Autowired
	@Qualifier("helloServicePrototype")
	private IHelloService helloServicePrototype2;

	@Autowired
	@Qualifier("helloService")
	private IHelloService helloService2;

	@GetMapping
	public String helloWorld() {
		helloService.getUsers();
		return "Hello World";
	}

	@GetMapping("/scope")
	public String scope() {
		log.info("helloService : {}", helloService);
		log.info("helloService2 : {}", helloService2);
		log.info("helloServiceProtoType : {}", helloServicePrototype);
		log.info("helloServicePrototype2 : {}", helloServicePrototype2);
		log.info("helloServiceRequest : {}", helloServiceRequest);
		return "Hello World";
	}
}