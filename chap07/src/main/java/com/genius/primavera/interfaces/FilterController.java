package com.genius.primavera.interfaces;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/lucy")
public class FilterController {

	@GetMapping("/filter")
	public String getMessage(String xss) {
		return xss;
	}

	@GetMapping("/filter/parameter/disable")
	public String filterParameterDisable(String message, String xss) {
		return message + xss;
	}

	@GetMapping("/filter/url/disable")
	public String filterUrlDisable(String message, String xss) {
		return message + xss;
	}

	@GetMapping("/filter/global")
	public String globalParameterDisable(String message, String global) {
		return message + global;
	}
}
