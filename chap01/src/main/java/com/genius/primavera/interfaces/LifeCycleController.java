package com.genius.primavera.interfaces;

import com.genius.primavera.lifecycle.AnnotationClass;
import com.genius.primavera.lifecycle.InterfaceImpl;
import com.genius.primavera.lifecycle.XmlBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/")
public class LifeCycleController {

	private final AnnotationClass annotationClass;
	private final InterfaceImpl anInterface;
	private final XmlBean xmlBean;

	@GetMapping("/life")
	public String scope() {
		return "Hello World";
	}
}