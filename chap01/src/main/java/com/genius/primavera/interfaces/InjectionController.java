package com.genius.primavera.interfaces;

import com.genius.primavera.application.injection.BooService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/")
public class InjectionController {

	private final BooService booService;

	@GetMapping("/overflow")
	public String cycleStackOverflow() {
		booService.boo();
		return "overflow";
	}
}
