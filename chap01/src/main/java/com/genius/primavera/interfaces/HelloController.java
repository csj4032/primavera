package com.genius.primavera.interfaces;

import com.genius.primavera.application.ScopeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/")
public class HelloController {

	private final ScopeService scopeService;
	private final ScopeService scopeServiceRequest;
	private final ScopeService scopeServiceProtoType;
	private final ScopeService scopeServiceRequestAnonymous;

	@Resource(name = "scopeServiceProtoType")
	private ScopeService scopeServiceProtoType2;

	@Resource(name = "scopeServiceRequest")
	private ScopeService scopeServiceRequest2;

	@Resource(name = "scopeService")
	private ScopeService scopeService2;

	@GetMapping
	public String helloWorld() {
		return "Hello World";
	}

	@GetMapping("/scope")
	public String scope() {
		log.info("scopeService1 : {}", scopeService);
		log.info("scopeService2 : {}", scopeService2);
		log.info("scopeServiceProtoType1 : {}", scopeServiceProtoType);
		log.info("scopeServiceProtoType2 : {}", scopeServiceProtoType2);
		log.info("scopeServiceRequest1 : {}", scopeServiceRequest);
		log.info("scopeServiceRequest2 : {}", scopeServiceRequest2);
		log.info("scopeServiceRequestAnonymous : {}", scopeServiceRequestAnonymous);
		return "Hello World";
	}
}