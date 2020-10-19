package com.genius.primavera.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//@Service
public class InjectionServiceCycle implements IInjectionServiceCycle {

//	@Autowired
//	private IInjectionService injectionService;

//	public InjectionServiceCycle(IInjectionService injectionService) {
//		this.injectionService = injectionService;
//	}

	@Override
	public String doSomething() {
		//injectionService.doSomething();
		return "InjectionServiceCycle";
	}
}