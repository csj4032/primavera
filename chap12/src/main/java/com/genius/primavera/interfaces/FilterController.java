package com.genius.primavera.interfaces;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/filter")
public class FilterController {

	@GetMapping
	public String getMessage(String xss) {
		return xss;
	}

	@GetMapping("/lucy/parameter/disable")
	public String filterParameterDisable(String message, String xss) {
		return message + xss;
	}

	@GetMapping("/lucy/url/disable")
	public String filterUrlDisable(String message, String xss) {
		return message + xss;
	}

	@GetMapping("/lucy/global")
	public String globalParameterDisable(String message, String global) {
		return message + global;
	}
}
