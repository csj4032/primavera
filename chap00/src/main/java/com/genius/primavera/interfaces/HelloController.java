package com.genius.primavera.interfaces;

import com.genius.primavera.application.GreetingService;
import com.genius.primavera.application.WorldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HelloController {

	private final GreetingService greetingService;
	private final WorldService worldService;

	@GetMapping(value = "/greeting")
	public String hello() {
		return greetingService.hello() + " " + worldService.world();
	}
}