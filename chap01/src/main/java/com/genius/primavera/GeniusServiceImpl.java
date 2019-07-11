package com.genius.primavera;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GeniusServiceImpl implements GeniusService {

	public void doSomething() {
		System.out.println("Genius Service");
	}
}