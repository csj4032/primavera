package com.genius.primavera.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InjectionService implements IInjectionService {

	//@Autowired(required = false)
	private InjectionServiceCycle injectionServiceCycle;

	//구동 시점에 NPE 확인, 순환참조 확인 가능
	@Autowired(required = false)
	public InjectionService(InjectionServiceCycle injectionServiceCycle) {
		this.injectionServiceCycle = injectionServiceCycle;
	}

	@Override
	public String doSomething() {
		log.info("A :" + injectionServiceCycle.doSomething());
		return "InjectionService";
	}
}