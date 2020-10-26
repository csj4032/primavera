package com.genius.primavera.application.injection;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BooServiceImpl implements BooService {

	// 생성자 순환참조 경고
	// Requested bean is currently in creation: Is there an unresolvable circular reference?
	//private final FooService fooService;

	// 호출시 경고
	// java.lang.StackOverflowError
	@Autowired
	private FooService fooService;

	@Override
	public String boo() {
		fooService.foo();
		return "boo";
	}
}
