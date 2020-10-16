package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class GeniusServiceImpl implements GeniusService {

	public void doSomething() {
		log.info("Genius Service");
	}
}