package com.genius.primavera.application.injection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FooServiceImpl implements FooService {

	private final BooService booService;

	@Override
	public String foo() {
		booService.boo();
		return "foo";
	}
}
