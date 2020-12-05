package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FrontController {

	private final FrontService frontService;
	private final Config config;

	@GetMapping(value = "/users/{userId}/orders")
	public FrontOrder getUserOrders(@PathVariable("userId") String userId) {
		FrontOrder frontOrder = frontService.findAllOrders(userId);
		return frontOrder;
	}

	@GetMapping(value = "/config")
	public String getConfig() {
		log.info("config : {}", config);
		return config.toString();
	}
}
